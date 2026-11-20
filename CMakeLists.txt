cmake_minimum_required(VERSION 3.21)
project(WineBridge VERSION 1.0.0 LANGUAGES CXX)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_AUTOMOC ON)
set(CMAKE_AUTOUIC ON)
set(CMAKE_AUTORCC ON)

find_package(Qt6 6.5 REQUIRED COMPONENTS Widgets)

qt_add_executable(winebridge
    src/main.cpp
)

target_link_libraries(winebridge PRIVATE Qt6::Widgets)
target_compile_options(winebridge PRIVATE -Wall -Wextra -Wpedantic)

install(TARGETS winebridge RUNTIME DESTINATION bin)
