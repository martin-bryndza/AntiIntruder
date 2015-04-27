@echo off 
echo Set oWS = WScript.CreateObject("WScript.Shell") > CreateShortcut.vbs
echo IF EXISTS "%USERPROFILE%\AppData\Roaming\Microsoft\Windows\Start Menu\Programs\Startup\" GOTO WIN7
echo sLinkFile = "%USERPROFILE%\Start Menu\Programs\Startup\AnyOffice-client.lnk" >> CreateShortcut.vbs
echo GOTO CONT
echo :WIN7
echo sLinkFile = "%USERPROFILE%\AppData\Roaming\Microsoft\Windows\Start Menu\Programs\Startup\AnyOffice-client.lnk" >> CreateShortcut.vbs
echo :CONT
echo Set oLink = oWS.CreateShortcut(sLinkFile) >> CreateShortcut.vbs
echo oLink.TargetPath = "/C:/Files/Diplomka/new/AntiIntruder/AntiIntruder-TrayApp/build/classes/main/anyoffice-client.jar" >> CreateShortcut.vbs
echo oLink.Save >> CreateShortcut.vbs
cscript CreateShortcut.vbs
del CreateShortcut.vbs