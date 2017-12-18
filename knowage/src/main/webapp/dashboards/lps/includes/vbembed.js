// This is a js1.1 code block, so make note that js1.1 is supported.
//* A_LZ_COPYRIGHT_BEGIN ******************************************************
//* Copyright 2001-2004 Laszlo Systems, Inc.  All Rights Reserved.            *
//* Use is subject to license terms.                                          *
//* A_LZ_COPYRIGHT_END ********************************************************
var jsVersion = 1.1;

function app_DoFSCommand(command, args) { 
    alert(command + ": " + args);
}
// Check the browser...we're looking for ie/win, but not aol
var isAOL = navigator.appVersion.indexOf("AOL") != -1;

var isOpera = navigator.userAgent.indexOf("Opera") != -1;
// True if we're on IE. By default, Opera (version 8.5) spoofs itself as IE.
var isIE = navigator.userAgent.indexOf("MSIE") != -1 && !isOpera;

// true if we're on windows
var isWin = navigator.appVersion.toLowerCase().indexOf("win") != -1;

// Write vbscript detection on ie win. IE on Windows doesn't support regular
// JavaScript plugins array detection.
if(isIE && isWin && !isAOL){
  document.write('<SCR' + 'IPT LANGUAGE=VBScript\> \n');
  document.write('On Error Resume Next \n');
  document.write('x = null \n');
  document.write('MM_FlashControlVersion = 0 \n');
  document.write('var VBFlashVer \n');
  document.write('For i = 9 To 1 Step -1 \n');
  document.write('	Set x = CreateObject("ShockwaveFlash.ShockwaveFlash." & i) \n');
  document.write('	MM_FlashControlInstalled = IsObject(x) \n');
  document.write('	If MM_FlashControlInstalled Then \n');
  document.write('		MM_FlashControlVersion = CStr(i) \n');
  document.write('		Exit For \n');
  document.write('	End If \n');
  document.write('Next \n');
  document.write('VBFlashVer = x.GetVariable("$version")\n');
  document.write('Sub app_FSCommand(ByVal command, ByVal args)\n');
  document.write('    call app_DoFSCommand(command, args)\n');
  document.write('end sub\n');
  document.write('<\/SCR' + 'IPT\> \n'); // break up end tag so it doesn't end our script
}
