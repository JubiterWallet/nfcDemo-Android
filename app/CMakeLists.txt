cmake_minimum_required(VERSION 3.6.0)

message("===============================>>> root SDK")

# **************************************************************************************************
#                                     相关变量设置
#  当需要对同一变量追加值时，不要直接使用 set(variable "new value"),这是值覆盖而不是追加。
#  例如：build.gradle 中已经存在 cppFlags 的值，现在需要在脚本中追加值 “-w”时，采用如下写法:
#        set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -w")
# **************************************************************************************************

# 不生成任何警告信息
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -w")

# **************************************************************************************************
#                            引入外部子目录（包含 CMakeLists.txt 的目录）
# 注意：子目录中编译库时一定要注意各个模块之间的依赖关系，如果出错，插起来会很麻烦。
#      add_subdirectory 必须要保证是子目录
# **************************************************************************************************

add_subdirectory(${CMAKE_CURRENT_SOURCE_DIR}/src/main/jni)
