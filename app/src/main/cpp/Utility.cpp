#include "Utility.h"
#include "AndroidOut.h"

#include <GLES3/gl3.h>
#include <algorithm>
#include <cmath>
#include <iterator>

#define CHECK_ERROR(e) case e: aout << "GL Error: "#e << std::endl; break;

bool Utility::checkAndLogGlError(bool alwaysLog) {
    GLenum error = glGetError();
    if (error == GL_NO_ERROR) {
        if (alwaysLog) {
            aout << "No GL error" << std::endl;
        }
        return true;
    } else {
        switch (error) {
            CHECK_ERROR(GL_INVALID_ENUM);
            CHECK_ERROR(GL_INVALID_VALUE);
            CHECK_ERROR(GL_INVALID_OPERATION);
            CHECK_ERROR(GL_INVALID_FRAMEBUFFER_OPERATION);
            CHECK_ERROR(GL_OUT_OF_MEMORY);
            default:
                aout << "Unknown GL error: " << error << std::endl;
        }
        return false;
    }
}

float *
Utility::buildOrthographicMatrix(float *outMatrix, float halfHeight, float aspect, float near,
                                 float far) {
    float halfWidth = halfHeight * aspect;

    // column 1
    outMatrix[0] = 1.f / halfWidth;
    outMatrix[1] = 0.f;
    outMatrix[2] = 0.f;
    outMatrix[3] = 0.f;

    // column 2
    outMatrix[4] = 0.f;
    outMatrix[5] = 1.f / halfHeight;
    outMatrix[6] = 0.f;
    outMatrix[7] = 0.f;

    // column 3
    outMatrix[8] = 0.f;
    outMatrix[9] = 0.f;
    outMatrix[10] = -2.f / (far - near);
    outMatrix[11] = -(far + near) / (far - near);

    // column 4
    outMatrix[12] = 0.f;
    outMatrix[13] = 0.f;
    outMatrix[14] = 0.f;
    outMatrix[15] = 1.f;

    return outMatrix;
}

float *Utility::buildIdentityMatrix(float *outMatrix) {
    // column 1
    outMatrix[0] = 1.f;
    outMatrix[1] = 0.f;
    outMatrix[2] = 0.f;
    outMatrix[3] = 0.f;

    // column 2
    outMatrix[4] = 0.f;
    outMatrix[5] = 1.f;
    outMatrix[6] = 0.f;
    outMatrix[7] = 0.f;

    // column 3
    outMatrix[8] = 0.f;
    outMatrix[9] = 0.f;
    outMatrix[10] = 1.f;
    outMatrix[11] = 0.f;

    // column 4
    outMatrix[12] = 0.f;
    outMatrix[13] = 0.f;
    outMatrix[14] = 0.f;
    outMatrix[15] = 1.f;

    return outMatrix;
}

float *Utility::buildPerspectiveMatrix(float *outMatrix, float fovYRadians, float aspect, float near,
                                       float far) {
    float f = 1.f / std::tan(fovYRadians / 2.f);

    outMatrix[0] = f / aspect;
    outMatrix[1] = 0.f;
    outMatrix[2] = 0.f;
    outMatrix[3] = 0.f;

    outMatrix[4] = 0.f;
    outMatrix[5] = f;
    outMatrix[6] = 0.f;
    outMatrix[7] = 0.f;

    outMatrix[8] = 0.f;
    outMatrix[9] = 0.f;
    outMatrix[10] = (far + near) / (near - far);
    outMatrix[11] = -1.f;

    outMatrix[12] = 0.f;
    outMatrix[13] = 0.f;
    outMatrix[14] = (2.f * far * near) / (near - far);
    outMatrix[15] = 0.f;

    return outMatrix;
}

float *Utility::multiplyMatrix(float *outMatrix, const float *lhs, const float *rhs) {
    float result[16];
    for (int col = 0; col < 4; ++col) {
        for (int row = 0; row < 4; ++row) {
            result[col * 4 + row] =
                    lhs[0 * 4 + row] * rhs[col * 4 + 0] +
                    lhs[1 * 4 + row] * rhs[col * 4 + 1] +
                    lhs[2 * 4 + row] * rhs[col * 4 + 2] +
                    lhs[3 * 4 + row] * rhs[col * 4 + 3];
        }
    }

    std::copy(std::begin(result), std::end(result), outMatrix);
    return outMatrix;
}

float *Utility::buildRotationMatrixX(float *outMatrix, float radians) {
    float s = std::sin(radians);
    float c = std::cos(radians);

    buildIdentityMatrix(outMatrix);
    outMatrix[5] = c;
    outMatrix[6] = s;
    outMatrix[9] = -s;
    outMatrix[10] = c;
    return outMatrix;
}

float *Utility::buildRotationMatrixY(float *outMatrix, float radians) {
    float s = std::sin(radians);
    float c = std::cos(radians);

    buildIdentityMatrix(outMatrix);
    outMatrix[0] = c;
    outMatrix[2] = -s;
    outMatrix[8] = s;
    outMatrix[10] = c;
    return outMatrix;
}
