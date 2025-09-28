#include "Renderer.h"

#include <game-activity/native_app_glue/android_native_app_glue.h>
#include <GLES3/gl3.h>
#include <algorithm>
#include <cmath>
#include <iterator>
#include <memory>
#include <sstream>
#include <vector>
#include <android/keycodes.h>

#include "AndroidOut.h"
#include "Shader.h"
#include "Utility.h"
#include "TextureAsset.h"

//! executes glGetString and outputs the result to logcat
#define PRINT_GL_STRING(s) {aout << #s": "<< glGetString(s) << std::endl;}

/*!
 * @brief if glGetString returns a space separated list of elements, prints each one on a new line
 *
 * This works by creating an istringstream of the input c-style string. Then that is used to create
 * a vector -- each element of the vector is a new element in the input string. Finally a foreach
 * loop consumes this and outputs it to logcat using @a aout
 */
#define PRINT_GL_STRING_AS_LIST(s) { \
std::istringstream extensionStream((const char *) glGetString(s));\
std::vector<std::string> extensionList(\
        std::istream_iterator<std::string>{extensionStream},\
        std::istream_iterator<std::string>());\
aout << #s":\n";\
for (auto& extension: extensionList) {\
    aout << extension << "\n";\
}\
aout << std::endl;\
}

//! Color for cornflower blue. Can be sent directly to glClearColor
#define CORNFLOWER_BLUE 100 / 255.f, 149 / 255.f, 237 / 255.f, 1

// Vertex shader, you'd typically load this from assets
static const char *vertex = R"vertex(#version 300 es
in vec3 inPosition;
in vec2 inUV;

out vec2 fragUV;
out vec3 fragNormal;

uniform mat4 uModel;
uniform mat4 uView;
uniform mat4 uProjection;

void main() {
    vec4 worldPos = uModel * vec4(inPosition, 1.0);
    mat3 normalMatrix = mat3(uView * uModel);
    fragNormal = normalize(normalMatrix * normalize(inPosition));
    fragUV = vec2(inUV.x, 1.0 - inUV.y);
    gl_Position = uProjection * uView * worldPos;
}
)vertex";

// Fragment shader, you'd typically load this from assets
static const char *fragment = R"fragment(#version 300 es
precision mediump float;

in vec2 fragUV;
in vec3 fragNormal;

uniform sampler2D uTexture;
uniform vec3 uLightDir;

out vec4 outColor;

void main() {
    vec3 baseColor = texture(uTexture, fragUV).rgb;
    vec3 normal = normalize(fragNormal);
    float diffuse = max(dot(normal, normalize(uLightDir)), 0.0);
    float ambient = 0.3;
    float brightness = clamp(ambient + diffuse * 0.7, 0.0, 1.0);
    vec3 litColor = baseColor * brightness;
    float rim = pow(1.0 - max(dot(normal, vec3(0.0, 0.0, -1.0)), 0.0), 2.0);
    litColor += vec3(0.05, 0.1, 0.2) * rim;
    outColor = vec4(litColor, 1.0);
}
)fragment";

static constexpr float kPi = 3.14159265358979323846f;
static constexpr float kFieldOfViewRadians = 60.f * kPi / 180.f;
static constexpr float kNearPlane = 0.1f;
static constexpr float kFarPlane = 20.f;
static constexpr float kCameraDistance = 3.0f;
static constexpr float kMaxPitchRadians = 1.3f;

Renderer::~Renderer() {
    if (display_ != EGL_NO_DISPLAY) {
        eglMakeCurrent(display_, EGL_NO_SURFACE, EGL_NO_SURFACE, EGL_NO_CONTEXT);
        if (context_ != EGL_NO_CONTEXT) {
            eglDestroyContext(display_, context_);
            context_ = EGL_NO_CONTEXT;
        }
        if (surface_ != EGL_NO_SURFACE) {
            eglDestroySurface(display_, surface_);
            surface_ = EGL_NO_SURFACE;
        }
        eglTerminate(display_);
        display_ = EGL_NO_DISPLAY;
    }
}

void Renderer::render() {
    // Check to see if the surface has changed size. This is _necessary_ to do every frame when
    // using immersive mode as you'll get no other notification that your renderable area has
    // changed.
    updateRenderArea();

    shader_->activate();

    // When the renderable area changes, the projection matrix has to also be updated.
    if (shaderNeedsNewProjectionMatrix_) {
        Utility::buildPerspectiveMatrix(
                projectionMatrix_.data(),
                kFieldOfViewRadians,
                float(width_) / float(height_),
                kNearPlane,
                kFarPlane);
        shaderNeedsNewProjectionMatrix_ = false;
        shader_->setProjectionMatrix(projectionMatrix_.data());
    }

    if (viewNeedsUpdate_) {
        Utility::buildIdentityMatrix(viewMatrix_.data());
        viewMatrix_[12] = 0.f;
        viewMatrix_[13] = 0.f;
        viewMatrix_[14] = -kCameraDistance;
        shader_->setViewMatrix(viewMatrix_.data());
        viewNeedsUpdate_ = false;
    }

    if (modelNeedsUpdate_) {
        float rotationX[16];
        float rotationY[16];
        Utility::buildRotationMatrixX(rotationX, rotationX_);
        Utility::buildRotationMatrixY(rotationY, rotationY_);
        Utility::multiplyMatrix(modelMatrix_.data(), rotationY, rotationX);
        shader_->setModelMatrix(modelMatrix_.data());
        modelNeedsUpdate_ = false;
    }

    const float lightDir[3] = {0.3f, 0.6f, -1.0f};
    shader_->setLightDirection(lightDir);

    // clear the buffers
    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

    // Render all the models.
    if (!models_.empty()) {
        for (const auto &model: models_) {
            shader_->drawModel(model);
        }
    }

    // Present the rendered image. This is an implicit glFlush.
    auto swapResult = eglSwapBuffers(display_, surface_);
    assert(swapResult == EGL_TRUE);
}

void Renderer::initRenderer() {
    // Choose your render attributes
    constexpr EGLint attribs[] = {
            EGL_RENDERABLE_TYPE, EGL_OPENGL_ES3_BIT,
            EGL_SURFACE_TYPE, EGL_WINDOW_BIT,
            EGL_BLUE_SIZE, 8,
            EGL_GREEN_SIZE, 8,
            EGL_RED_SIZE, 8,
            EGL_DEPTH_SIZE, 24,
            EGL_NONE
    };

    // The default display is probably what you want on Android
    auto display = eglGetDisplay(EGL_DEFAULT_DISPLAY);
    eglInitialize(display, nullptr, nullptr);

    // figure out how many configs there are
    EGLint numConfigs;
    eglChooseConfig(display, attribs, nullptr, 0, &numConfigs);

    // get the list of configurations
    std::unique_ptr<EGLConfig[]> supportedConfigs(new EGLConfig[numConfigs]);
    eglChooseConfig(display, attribs, supportedConfigs.get(), numConfigs, &numConfigs);

    // Find a config we like.
    // Could likely just grab the first if we don't care about anything else in the config.
    // Otherwise hook in your own heuristic
    auto config = *std::find_if(
            supportedConfigs.get(),
            supportedConfigs.get() + numConfigs,
            [&display](const EGLConfig &config) {
                EGLint red, green, blue, depth;
                if (eglGetConfigAttrib(display, config, EGL_RED_SIZE, &red)
                    && eglGetConfigAttrib(display, config, EGL_GREEN_SIZE, &green)
                    && eglGetConfigAttrib(display, config, EGL_BLUE_SIZE, &blue)
                    && eglGetConfigAttrib(display, config, EGL_DEPTH_SIZE, &depth)) {

                    aout << "Found config with " << red << ", " << green << ", " << blue << ", "
                         << depth << std::endl;
                    return red == 8 && green == 8 && blue == 8 && depth == 24;
                }
                return false;
            });

    aout << "Found " << numConfigs << " configs" << std::endl;
    aout << "Chose " << config << std::endl;

    // create the proper window surface
    EGLint format;
    eglGetConfigAttrib(display, config, EGL_NATIVE_VISUAL_ID, &format);
    EGLSurface surface = eglCreateWindowSurface(display, config, app_->window, nullptr);

    // Create a GLES 3 context
    EGLint contextAttribs[] = {EGL_CONTEXT_CLIENT_VERSION, 3, EGL_NONE};
    EGLContext context = eglCreateContext(display, config, nullptr, contextAttribs);

    // get some window metrics
    auto madeCurrent = eglMakeCurrent(display, surface, surface, context);
    assert(madeCurrent);

    display_ = display;
    surface_ = surface;
    context_ = context;

    // make width and height invalid so it gets updated the first frame in @a updateRenderArea()
    width_ = -1;
    height_ = -1;

    PRINT_GL_STRING(GL_VENDOR);
    PRINT_GL_STRING(GL_RENDERER);
    PRINT_GL_STRING(GL_VERSION);
    PRINT_GL_STRING_AS_LIST(GL_EXTENSIONS);

    shader_ = std::unique_ptr<Shader>(
            Shader::loadShader(
                    vertex,
                    fragment,
                    "inPosition",
                    "inUV",
                    "uModel",
                    "uView",
                    "uProjection",
                    "uLightDir",
                    "uTexture"));
    assert(shader_);

    // Note: there's only one shader in this demo, so I'll activate it here. For a more complex game
    // you'll want to track the active shader and activate/deactivate it as necessary
    shader_->activate();

    // setup any other gl related global states
    glClearColor(CORNFLOWER_BLUE);

    glEnable(GL_DEPTH_TEST);
    glDepthFunc(GL_LEQUAL);

    // enable alpha globally for now, you probably don't want to do this in a game
    glEnable(GL_BLEND);
    glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

    // get some demo models into memory
    createModels();
}

void Renderer::updateRenderArea() {
    EGLint width;
    eglQuerySurface(display_, surface_, EGL_WIDTH, &width);

    EGLint height;
    eglQuerySurface(display_, surface_, EGL_HEIGHT, &height);

    if (width != width_ || height != height_) {
        width_ = width;
        height_ = height;
        glViewport(0, 0, width, height);

        // make sure that we lazily recreate the projection matrix before we render
        shaderNeedsNewProjectionMatrix_ = true;
    }
}

/**
 * @brief Create any demo models we want for this demo.
 */
void Renderer::createModels() {
    const int latSegments = 64;
    const int lonSegments = 128;

    std::vector<Vertex> vertices;
    vertices.reserve((latSegments + 1) * (lonSegments + 1));

    for (int lat = 0; lat <= latSegments; ++lat) {
        float v = static_cast<float>(lat) / static_cast<float>(latSegments);
        float theta = v * kPi;
        float sinTheta = std::sin(theta);
        float cosTheta = std::cos(theta);

        for (int lon = 0; lon <= lonSegments; ++lon) {
            float u = static_cast<float>(lon) / static_cast<float>(lonSegments);
            float phi = u * 2.f * kPi;
            float sinPhi = std::sin(phi);
            float cosPhi = std::cos(phi);

            Vector3 position{
                    sinTheta * cosPhi,
                    cosTheta,
                    sinTheta * sinPhi
            };
            Vector2 uv{u, v};
            vertices.emplace_back(position, uv);
        }
    }

    std::vector<Index> indices;
    indices.reserve(latSegments * lonSegments * 6);
    int rowStride = lonSegments + 1;
    for (int lat = 0; lat < latSegments; ++lat) {
        for (int lon = 0; lon < lonSegments; ++lon) {
            Index topLeft = static_cast<Index>(lat * rowStride + lon);
            Index topRight = static_cast<Index>(topLeft + 1);
            Index bottomLeft = static_cast<Index>((lat + 1) * rowStride + lon);
            Index bottomRight = static_cast<Index>(bottomLeft + 1);

            indices.push_back(topLeft);
            indices.push_back(bottomLeft);
            indices.push_back(topRight);

            indices.push_back(topRight);
            indices.push_back(bottomLeft);
            indices.push_back(bottomRight);
        }
    }

    auto spEarthTexture = TextureAsset::createProceduralEarthTexture();

    models_.emplace_back(std::move(vertices), std::move(indices), spEarthTexture);
}

void Renderer::handleInput() {
    // handle all queued inputs
    auto *inputBuffer = android_app_swap_input_buffers(app_);
    if (!inputBuffer) {
        // no inputs yet.
        return;
    }

    // handle motion events (motionEventsCounts can be 0).
    for (auto i = 0; i < inputBuffer->motionEventsCount; i++) {
        auto &motionEvent = inputBuffer->motionEvents[i];
        auto action = motionEvent.action;
        auto pointerIndex = (action & AMOTION_EVENT_ACTION_POINTER_INDEX_MASK)
                >> AMOTION_EVENT_ACTION_POINTER_INDEX_SHIFT;

        switch (action & AMOTION_EVENT_ACTION_MASK) {
            case AMOTION_EVENT_ACTION_DOWN:
            case AMOTION_EVENT_ACTION_POINTER_DOWN: {
                auto &pointer = motionEvent.pointers[pointerIndex];
                if (activePointerId_ == -1) {
                    activePointerId_ = pointer.id;
                    lastTouchX_ = GameActivityPointerAxes_getX(&pointer);
                    lastTouchY_ = GameActivityPointerAxes_getY(&pointer);
                }
                break;
            }
            case AMOTION_EVENT_ACTION_CANCEL:
            case AMOTION_EVENT_ACTION_UP:
            case AMOTION_EVENT_ACTION_POINTER_UP: {
                auto &pointer = motionEvent.pointers[pointerIndex];
                if (pointer.id == activePointerId_) {
                    activePointerId_ = -1;
                }
                break;
            }
            case AMOTION_EVENT_ACTION_MOVE: {
                if (activePointerId_ == -1) {
                    break;
                }

                for (auto index = 0; index < motionEvent.pointerCount; index++) {
                    auto &pointer = motionEvent.pointers[index];
                    if (pointer.id == activePointerId_) {
                        float x = GameActivityPointerAxes_getX(&pointer);
                        float y = GameActivityPointerAxes_getY(&pointer);
                        float dx = x - lastTouchX_;
                        float dy = y - lastTouchY_;
                        lastTouchX_ = x;
                        lastTouchY_ = y;

                        int width = std::max(width_, 1);
                        int height = std::max(height_, 1);
                        rotationY_ += (dx / static_cast<float>(width)) * 2.f * kPi;
                        rotationX_ += (dy / static_cast<float>(height)) * kPi;

                        rotationX_ = std::clamp(rotationX_, -kMaxPitchRadians, kMaxPitchRadians);
                        if (rotationY_ > kPi) {
                            rotationY_ -= 2.f * kPi;
                        } else if (rotationY_ < -kPi) {
                            rotationY_ += 2.f * kPi;
                        }

                        modelNeedsUpdate_ = true;
                        break;
                    }
                }
                break;
            }
            default:
                break;
        }
    }
    // clear the motion input count in this buffer for main thread to re-use.
    android_app_clear_motion_events(inputBuffer);

    // handle input key events.
    for (auto i = 0; i < inputBuffer->keyEventsCount; i++) {
        auto &keyEvent = inputBuffer->keyEvents[i];
        if (keyEvent.action == AKEY_EVENT_ACTION_DOWN && keyEvent.keyCode == AKEYCODE_BACK) {
            app_->destroyRequested = 1;
        }
    }
    // clear the key input count too.
    android_app_clear_key_events(inputBuffer);
}
