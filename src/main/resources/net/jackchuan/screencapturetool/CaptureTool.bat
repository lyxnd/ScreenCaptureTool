@echo off
REM 设置最小和最大堆内存
set MIN_HEAP=16m
set MAX_HEAP=1024m

REM 启动你的模块主类
bin\javaw.exe -Xms%MIN_HEAP% -Xmx%MAX_HEAP% -m net.jackchuan.screencapturetool/net.jackchuan.screencapturetool.ScreenCaptureToolApp
