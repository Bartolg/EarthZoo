#include <android/imagedecoder.h>
#include <algorithm>
#include <cassert>
#include <cmath>
#include <cstdint>
#include <memory>
#include <vector>

#include "TextureAsset.h"

namespace {

GLuint createTextureFromPixels(const uint8_t *data, int width, int height) {
    GLuint textureId;
    glGenTextures(1, &textureId);
    glBindTexture(GL_TEXTURE_2D, textureId);

    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);

    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

    glTexImage2D(
            GL_TEXTURE_2D,
            0,
            GL_RGBA,
            width,
            height,
            0,
            GL_RGBA,
            GL_UNSIGNED_BYTE,
            data);

    glGenerateMipmap(GL_TEXTURE_2D);

    return textureId;
}

} // namespace

std::shared_ptr<TextureAsset>
TextureAsset::loadAsset(AAssetManager *assetManager, const std::string &assetPath) {
    // Get the image from asset manager
    assert(assetManager != nullptr);

    auto pAsset = AAssetManager_open(
            assetManager,
            assetPath.c_str(),
            AASSET_MODE_BUFFER);
    assert(pAsset != nullptr);

    // Make a decoder to turn it into a texture
    AImageDecoder *pAndroidDecoder = nullptr;
    auto result = AImageDecoder_createFromAAsset(pAsset, &pAndroidDecoder);
    assert(result == ANDROID_IMAGE_DECODER_SUCCESS);

    // make sure we get 8 bits per channel out. RGBA order.
    AImageDecoder_setAndroidBitmapFormat(pAndroidDecoder, ANDROID_BITMAP_FORMAT_RGBA_8888);

    // Get the image header, to help set everything up
    const AImageDecoderHeaderInfo *pAndroidHeader = nullptr;
    pAndroidHeader = AImageDecoder_getHeaderInfo(pAndroidDecoder);

    // important metrics for sending to GL
    auto width = AImageDecoderHeaderInfo_getWidth(pAndroidHeader);
    auto height = AImageDecoderHeaderInfo_getHeight(pAndroidHeader);
    auto stride = AImageDecoder_getMinimumStride(pAndroidDecoder);

    // Get the bitmap data of the image
    auto upAndroidImageData = std::make_unique<std::vector<uint8_t>>(height * stride);
    auto decodeResult = AImageDecoder_decodeImage(
            pAndroidDecoder,
            upAndroidImageData->data(),
            stride,
            upAndroidImageData->size());
    assert(decodeResult == ANDROID_IMAGE_DECODER_SUCCESS);

    auto textureId = createTextureFromPixels(upAndroidImageData->data(), width, height);

    // cleanup helpers
    AImageDecoder_delete(pAndroidDecoder);
    AAsset_close(pAsset);

    return std::shared_ptr<TextureAsset>(new TextureAsset(textureId));
}

TextureAsset::~TextureAsset() {
    // return texture resources
    glDeleteTextures(1, &textureID_);
    textureID_ = 0;
}

std::shared_ptr<TextureAsset> TextureAsset::createProceduralEarthTexture() {
    constexpr int width = 256;
    constexpr int height = 128;
    constexpr float kPi = 3.14159265358979323846f;
    constexpr float kTwoPi = 2.f * kPi;
    constexpr float kHalfPi = 0.5f * kPi;

    std::vector<uint8_t> pixels(width * height * 4);

    for (int y = 0; y < height; ++y) {
        float v = static_cast<float>(y) / static_cast<float>(height - 1);
        float latitude = (v * kPi) - kHalfPi;
        float latCos = std::cos(latitude);

        for (int x = 0; x < width; ++x) {
            float u = static_cast<float>(x) / static_cast<float>(width - 1);
            float longitude = (u * kTwoPi) - kPi;

            float ridge = std::sin(latitude * 3.5f + std::cos(longitude * 2.8f)) * 0.5f;
            float swirl = std::sin(longitude * 1.5f + latitude * 2.3f);
            float continentMask = latCos * 0.45f + ridge * 0.35f + swirl * 0.2f;

            float coastline = std::sin(latitude * 12.f) * std::sin(longitude * 6.f) * 0.15f;
            bool isLand = (continentMask + coastline) > 0.08f;

            float iceAmount = std::clamp((std::fabs(latitude) - 1.0f) * 1.5f, 0.0f, 1.0f);

            float r;
            float g;
            float b;

            if (isLand) {
                float elevation = std::sin(latitude * 5.0f + longitude * 1.7f) * 0.5f + 0.5f;
                float moisture = std::sin(longitude * 2.4f - latitude * 1.9f) * 0.5f + 0.5f;
                float grassy = std::clamp(0.4f + moisture * 0.4f - elevation * 0.2f, 0.0f, 1.0f);
                float desert = std::clamp(elevation * 0.6f - moisture * 0.5f + 0.3f, 0.0f, 1.0f);
                float mountain = std::clamp(elevation * 1.2f - 0.6f, 0.0f, 1.0f);

                r = 0.08f + grassy * 0.25f + desert * 0.45f + mountain * 0.25f;
                g = 0.16f + grassy * 0.55f + desert * 0.38f + mountain * 0.25f;
                b = 0.06f + grassy * 0.20f + desert * 0.20f + mountain * 0.25f;

                float coastalInfluence = std::clamp(0.3f - (continentMask + coastline - 0.08f), 0.0f, 0.3f) / 0.3f;
                float blend = coastalInfluence * 0.4f;
                r += (0.12f - r) * blend;
                g += (0.25f - g) * blend;
                b += (0.35f - b) * blend;
            } else {
                float depth = 0.5f + std::sin(latitude * 4.3f + longitude * 0.9f) * 0.25f;
                float current = std::sin(longitude * 3.1f) * std::sin(latitude * 2.7f);
                float turbulence = std::sin((longitude + latitude) * 5.7f) * 0.1f;

                float ocean = depth + current * 0.15f + turbulence;
                ocean = std::clamp(ocean, 0.0f, 1.0f);

                r = 0.02f + ocean * 0.14f;
                g = 0.09f + ocean * 0.32f;
                b = 0.18f + ocean * 0.55f;
            }

            r = std::clamp(r + iceAmount * 0.6f, 0.0f, 1.0f);
            g = std::clamp(g + iceAmount * 0.65f, 0.0f, 1.0f);
            b = std::clamp(b + iceAmount * 0.7f, 0.0f, 1.0f);

            float highlight = std::clamp(0.15f + latCos * 0.15f, 0.0f, 1.0f);
            r = std::clamp(r + highlight * 0.05f, 0.0f, 1.0f);
            g = std::clamp(g + highlight * 0.04f, 0.0f, 1.0f);
            b = std::clamp(b + highlight * 0.03f, 0.0f, 1.0f);

            size_t index = static_cast<size_t>(y * width + x) * 4;
            pixels[index + 0] = static_cast<uint8_t>(std::clamp(r, 0.0f, 1.0f) * 255.0f);
            pixels[index + 1] = static_cast<uint8_t>(std::clamp(g, 0.0f, 1.0f) * 255.0f);
            pixels[index + 2] = static_cast<uint8_t>(std::clamp(b, 0.0f, 1.0f) * 255.0f);
            pixels[index + 3] = 255;
        }
    }

    auto textureId = createTextureFromPixels(pixels.data(), width, height);
    return std::shared_ptr<TextureAsset>(new TextureAsset(textureId));
}
