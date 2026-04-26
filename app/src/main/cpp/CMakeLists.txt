cmake_minimum_required(VERSION 3.22.1)
project("securelock")

add_library(securelock SHARED secure_hash.cpp)
target_link_libraries(securelock android log)
