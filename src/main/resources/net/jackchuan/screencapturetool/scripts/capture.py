import sys
import os
import tempfile
from PIL import ImageGrab

def capture_screen(x, y, width, height):
    # 截取屏幕指定区域
    bbox = (x, y, x + width, y + height)
    screenshot = ImageGrab.grab(bbox)
    # 使用临时文件夹保存
    temp_dir = tempfile.gettempdir()
    output_path = os.path.join(temp_dir, "screenshot.png")
    screenshot.save(output_path, format="PNG")
    print(output_path)  # 输出保存路径
    return output_path

if __name__ == "__main__":
    if len(sys.argv) != 5:
        print("Usage: python capture_screen.py <x> <y> <width> <height>", file=sys.stderr)
        sys.exit(1)

    # 获取参数
    x = int(sys.argv[1])
    y = int(sys.argv[2])
    width = int(sys.argv[3])
    height = int(sys.argv[4])

    try:
        capture_screen(x, y, width, height)
    except Exception as e:
        print(f"Error: {e}", file=sys.stderr)
        sys.exit(2)
