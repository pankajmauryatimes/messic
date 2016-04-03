!addplugindir ".\Include\ZipDLL\"
!addplugindir ".\Include\AccessControl\Plugins\"
!include .\Include\ZipDLL\zipdll.nsh


# This installs two files, app.exe and logo.ico, creates a start menu shortcut, builds an uninstaller, and
# adds uninstall information to the registry for Add/Remove Programs
 
# To get started, put this script into a folder with the two files (app.exe, logo.ico, and license.rtf -
# You'll have to create these yourself) and run makensis on it
 
# If you change the names "app.exe", "logo.ico", or "license.rtf" you should do a search and replace - they
# show up in a few places.
# All the other settings can be tweaked by editing the !defines at the top of this script
!define APPNAME "messic"
!define COMPANYNAME "messic"
!define DESCRIPTION "Music organizer and player"
#Files to include at the installer
!define ZIP_FILE "messic-1.1.0.app.zip"
# These three must be integers
!define VERSIONMAJOR 1
!define VERSIONMINOR 1
!define VERSIONBUILD 0
# These will be displayed by the "Click here for support information" link in "Add/Remove Programs"
# It is possible to use "mailto:" links in here to open the email client
!define HELPURL "http://spheras.github.io/messic/" # "Support Information" link
!define UPDATEURL "http://spheras.github.io/messic/" # "Product Updates" link
!define ABOUTURL "http://spheras.github.io/messic/" # "Publisher" link
!define LOGOTIPO_FILE "logotipo-32x32.ico"
# This is the size (in kB) of all the files copied into "Program Files"
!define INSTALLSIZE 122880
 
XPStyle on
RequestExecutionLevel admin ;Require admin rights on NT6+ (When UAC is turned on)

; First is default
LoadLanguageFile "${NSISDIR}\Contrib\Language files\English.nlf"
LoadLanguageFile "${NSISDIR}\Contrib\Language files\Spanish.nlf"
LoadLanguageFile "${NSISDIR}\Contrib\Language files\Catalan.nlf"

LangString uninstall_previous_message ${LANG_ENGLISH} "${APPNAME} is already installed. $\n$\nClick 'OK' to remove the previous version (remember that the uninstall DON'T remove your music folder) or `Cancel` to cancel this upgrade. $\nWARNING: before uninstall, be sure to STOP the messic service if it is running."
LangString uninstall_previous_message ${LANG_SPANISH} "${APPNAME} ya est� instalado. $\n$\nHaz Click en 'OK' para eliminar la instalaci�n previa de messic (recuerda que la desinstalaci�n NO elimina la carpeta de m�sica) o 'Cancelar' para cancelar esta instalaci�n. $\nIMPORTANTE: antes de desinstalar aseg�rate de que el servicio messic est� parado."
LangString uninstall_previous_message ${LANG_CATALAN} "${APPNAME} ja est� instal�lat. $\n$\nFes clic a 'OK' per eliminar la instal�laci� pr�via d'messic (recorda que la desinstal�laci� NO elimina la carpeta de m�sica) o 'Cancel�la' per cancel�lar aquesta instal�laci�. $\nIMPORTANT: abans de desinstal�lar assegura't que el servei messic est� aturat."

 
InstallDir "$PROGRAMFILES\${APPNAME}"
 
# rtf or txt file - remember if it is txt, it must be in the DOS text format (\r\n)
LicenseData "license.txt"
# This will be in the installer/uninstaller's title bar
Name "${APPNAME}"
Icon "${LOGOTIPO_FILE}"
outFile "messic-installer.exe"
 
!include LogicLib.nsh
 
# Just three pages - license agreement, install location, and installation
page license
page directory
Page instfiles

 
!macro VerifyUserIsAdmin
UserInfo::GetAccountType
pop $0
${If} $0 != "admin" ;Require admin rights on NT4+
        messageBox mb_iconstop "Administrator rights required!"
        setErrorLevel 740 ;ERROR_ELEVATION_REQUIRED
        quit
${EndIf}
!macroend
 
function .onInit
	setShellVarContext all
	!insertmacro VerifyUserIsAdmin

    ;code to uninstall previously the installed messic	
	ReadRegStr $R0 HKLM \
	  "Software\Microsoft\Windows\CurrentVersion\Uninstall\${COMPANYNAME} ${APPNAME}" \
	  "UninstallString"
	  StrCmp $R0 "" done
	 
	  MessageBox MB_OKCANCEL|MB_ICONEXCLAMATION \
	  "$(uninstall_previous_message)" \
	  IDOK uninst
	  Abort
	 
	;Run the uninstaller
	uninst:
	  ClearErrors
		;Exec $INSTDIR\uninstall.exe ; instead of the ExecWait line	  
	  ExecWait '$R0 _?=$INSTDIR' ;Do not copy the uninstaller to a temp file
	 
	  IfErrors no_remove_uninstaller done
		;You can either use Delete /REBOOTOK in the uninstaller or add some code
		;here to remove the uninstaller. Use a registry key to check
		;whether the user has chosen to uninstall. If you are using an uninstaller
		;components page, make sure all sections are uninstalled.
	  no_remove_uninstaller:
	 
	done:

	;we check again
	ReadRegStr $R0 HKLM \
	  "Software\Microsoft\Windows\CurrentVersion\Uninstall\${COMPANYNAME} ${APPNAME}" \
	  "UninstallString"
	StrCmp $R0 "" doneagain
	Abort

	doneagain:
	
functionEnd
 
section "install"
	# Files for the install directory - to build the installer, these should be in the same directory as the install script (this file)
	setOutPath $INSTDIR
	# Files added here should be removed by the uninstaller (see section "uninstall")
	file "${ZIP_FILE}"
	file "${LOGOTIPO_FILE}"
	# Add any other files for the install directory (license files, app data, etc) here
 
	# Decompressing the zip file
	
	ZipDLL::extractall "${ZIP_FILE}" "$INSTDIR\messic.app"
  	delete ${ZIP_FILE}
	# Make the directory "$INSTDIR\database" read write accessible by all users
	AccessControl::GrantOnFile \
    "$INSTDIR\messic.app" "(BU)" "GenericRead + GenericWrite"

	# Uninstaller - See function un.onInit and section "uninstall" for configuration
	writeUninstaller "$INSTDIR\uninstall.exe"
 
	# Start Menu
	createDirectory "$SMPROGRAMS\${COMPANYNAME}"
	setOutPath $INSTDIR\messic.app #this to set the start in parameter of the shortcut
	createShortCut "$SMPROGRAMS\${COMPANYNAME}\${APPNAME}.lnk" "$INSTDIR\messic.app\messic.exe" "" "$INSTDIR\${LOGOTIPO_FILE}"
	setOutPath $INSTDIR #returning to the start in of the application
	createShortCut "$SMPROGRAMS\${COMPANYNAME}\uninstall.lnk" "$INSTDIR\uninstall.exe" "" "$INSTDIR\uninstall.exe"
 
	# Registry information for add/remove programs
	WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${COMPANYNAME} ${APPNAME}" "DisplayName" "${COMPANYNAME} - ${APPNAME} - ${DESCRIPTION}"
	WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${COMPANYNAME} ${APPNAME}" "UninstallString" "$\"$INSTDIR\uninstall.exe$\""
	WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${COMPANYNAME} ${APPNAME}" "QuietUninstallString" "$\"$INSTDIR\uninstall.exe$\" /S"
	WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${COMPANYNAME} ${APPNAME}" "InstallLocation" "$\"$INSTDIR$\""
	WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${COMPANYNAME} ${APPNAME}" "DisplayIcon" "$\"$INSTDIR\${LOGOTIPO_FILE}$\""
	WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${COMPANYNAME} ${APPNAME}" "Publisher" "$\"${COMPANYNAME}$\""
	WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${COMPANYNAME} ${APPNAME}" "HelpLink" "$\"${HELPURL}$\""
	WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${COMPANYNAME} ${APPNAME}" "URLUpdateInfo" "$\"${UPDATEURL}$\""
	WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${COMPANYNAME} ${APPNAME}" "URLInfoAbout" "$\"${ABOUTURL}$\""
	WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${COMPANYNAME} ${APPNAME}" "DisplayVersion" "$\"${VERSIONMAJOR}.${VERSIONMINOR}.${VERSIONBUILD}$\""
	WriteRegDWORD HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${COMPANYNAME} ${APPNAME}" "VersionMajor" ${VERSIONMAJOR}
	WriteRegDWORD HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${COMPANYNAME} ${APPNAME}" "VersionMinor" ${VERSIONMINOR}
	# There is no option for modifying or repairing the install
	WriteRegDWORD HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${COMPANYNAME} ${APPNAME}" "NoModify" 1
	WriteRegDWORD HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${COMPANYNAME} ${APPNAME}" "NoRepair" 1
	# Set the INSTALLSIZE constant (!defined at the top of this script) so Add/Remove Programs can accurately report the size
	WriteRegDWORD HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${COMPANYNAME} ${APPNAME}" "EstimatedSize" ${INSTALLSIZE}
sectionEnd
 
# Uninstaller
 
function un.onInit
	SetShellVarContext all
 
	#Verify the uninstaller - last chance to back out
	MessageBox MB_OKCANCEL "Permanently remove ${APPNAME}?" IDOK next
		Abort
	next:
	!insertmacro VerifyUserIsAdmin
functionEnd
 
section "uninstall"
	# Remove Start Menu launcher
	delete "$SMPROGRAMS\${COMPANYNAME}\${APPNAME}.lnk"
	delete "$SMPROGRAMS\${COMPANYNAME}\uninstall.lnk"
	# Try to remove the Start Menu folder - this will only happen if it is empty
	rmDir "$SMPROGRAMS\${COMPANYNAME}"
 
	# Remove files
	#delete $INSTDIR\app.exe
	delete $INSTDIR\${LOGOTIPO_FILE}
 
	# Always delete uninstaller as the last action
	delete $INSTDIR\uninstall.exe
 
	# removing the whole directory
	rmDir /r $INSTDIR\messic.app
	# Try to remove the install directory - this will only happen if it is empty
	rmDir $INSTDIR
 
	# Remove uninstaller information from the registry
	DeleteRegKey HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${COMPANYNAME} ${APPNAME}"
sectionEnd