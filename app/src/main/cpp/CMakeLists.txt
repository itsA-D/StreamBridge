cmake_minimum_required(VERSION 3.10.2)
project("edge_detection")

# Set OpenCV directory: edit this path to your OpenCV Android SDK 'sdk/native/jni' directory
# Example: set(OpenCV_DIR /home/user/OpenCV-android-sdk/sdk/native/jni)
# Then find_package(OpenCV REQUIRED)
if(NOT DEFINED OpenCV_DIR)
    message(WARNING "OpenCV_DIR not defined. Edit CMakeLists.txt to point to your OpenCV Android SDK sdk/native/jni folder.")
endif()

find_package(OpenCV REQUIRED QUIET)
message(STATUS "OpenCV include: ${OpenCV_INCLUDE_DIRS}")

add_library(native-lib SHARED native-lib.cpp)

include_directories(${OpenCV_INCLUDE_DIRS})
target_link_libraries(native-lib ${OpenCV_LIBS} android log)
