@echo off
setlocal
set "PATH=C:\Qt\6.10.3\msvc2022_64\bin;%PATH%"
cd /d "%~dp0build"
if exist "ReportItAdmin.exe" (
    start "" "ReportItAdmin.exe"
) else (
    echo ReportItAdmin.exe not found. Please run build.bat first.
)
