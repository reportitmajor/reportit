@echo off
setlocal

echo [1/3] Initializing MSVC x64 Environment...
call "C:\Program Files (x86)\Microsoft Visual Studio\2022\BuildTools\VC\Auxiliary\Build\vcvarsall.bat" x64
if %ERRORLEVEL% neq 0 (
    echo Failed to initialize MSVC environment.
    exit /b %ERRORLEVEL%
)

set "PATH=C:\Qt\6.10.3\msvc2022_64\bin;%PATH%"

echo [2/3] Configuring CMake...
cmake -B build -G "Ninja" -DCMAKE_PREFIX_PATH="C:/Qt/6.10.3/msvc2022_64" -DCMAKE_BUILD_TYPE=Release
if %ERRORLEVEL% neq 0 (
    echo CMake configuration failed.
    exit /b %ERRORLEVEL%
)

echo [3/3] Compiling ReportItAdmin...
cmake --build build --config Release
if %ERRORLEVEL% neq 0 (
    echo Build failed.
    exit /b %ERRORLEVEL%
)

echo Build complete! Executable located at: build\ReportItAdmin.exe
