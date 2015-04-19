@echo off
IF EXIST "%USERPROFILE%\AppData\Roaming\Microsoft\Windows\Start Menu\Programs\Startup" GOTO WIN7
del "%USERPROFILE%\Start Menu\Programs\Startup\AnyOffice-client.lnk"
GOTO END
:WIN7
del "%USERPROFILE%\AppData\Roaming\Microsoft\Windows\Start Menu\Programs\Startup\AnyOffice-client.lnk"
:END
