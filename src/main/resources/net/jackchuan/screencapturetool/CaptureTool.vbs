Set fso = CreateObject("Scripting.FileSystemObject")
Set shell = CreateObject("WScript.Shell")

' 获取 VBS 当前目录
scriptPath = fso.GetParentFolderName(WScript.ScriptFullName)

' 切换当前目录
shell.CurrentDirectory = scriptPath

' 正确拼接路径并执行 .bat（隐藏窗口）
shell.Run Chr(34) & scriptPath & "\CaptureTool.bat" & Chr(34), 0
