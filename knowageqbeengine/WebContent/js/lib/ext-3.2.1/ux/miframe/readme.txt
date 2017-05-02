 ************************************************************************************
 * Author: Doug Hendricks. doug[always-At]theactivegroup.com
 * Copyright 2007-2010, Active Group, Inc.  All rights reserved.
 ************************************************************************************
  
 ManagedIFrame (MIF 2.1)
 For Ext 3.1 or higher ONLY.
 
 MIF 2.x is structured to permit custom builds (using the Ext JSBuilder versions 1.x and 2).
 The MIF distribution should contain a standard build of miframe.js and 
 miframe-debug.js designed for typical use with the Ext 3.1 framework.
 
************************************************************************************
Recommended development(debugging) tag configuration:
<head>
 <link rel="stylesheet" type="text/css" href="../lib/ext-3.1/resources/css/ext-all.css" />
 <script type="text/javascript" src="../lib/ext-3.1/adapter/ext/ext-base.js"></script>
 <script type="text/javascript" src="../lib/ext-3.1/ext-all[-debug].js"></script>
 <script type="text/javascript" src="../lib/ux/mif/miframe[-debug].js"></script>
</head>

************************************************************************************ 
Additional MIF modules may be loaded if feature support is desired for X-frame messaging:

<head>
 <link rel="stylesheet" type="text/css" href="../lib/ext-3.1+/resources/css/ext-all.css" />
 <script type="text/javascript" src="../lib/ext-3.1/adapter/ext/ext-base.js"></script>
 <script type="text/javascript" src="../lib/ext-3.1/ext-all[-debug].js"></script>
 <script type="text/javascript" src="../lib/ux/mif/miframe[-debug].js"></script>
 <script type="text/javascript" src="../lib/ux/mif/mifmsg.js"></script>
</head>

Note: multidom.js is a required module for any MIF configuration.
 
************************************************************************************ 
Optionally, individual source modules may be loaded (but should be deployed in the following order)
 <head>
  <link rel="stylesheet" type="text/css" href="../lib/ext-3.1/resources/css/ext-all.css" />
  <script type="text/javascript" src="../lib/ext-3.1/adapter/ext/ext-base.js"></script>
  <script type="text/javascript" src="../lib/ext-3.1/ext-all[-debug].js"></script>
  <script type="text/javascript" src="../lib/ux/mif/uxvismode.js"></script>
  <script type="text/javascript" src="../lib/ux/mif/multidom.js"></script>
  <script type="text/javascript" src="../lib/ux/mif/mif.js"></script>
  <script type="text/javascript" src="../lib/ux/mif/mifmsg.js"></script>
  
 </head>
 
 The included miframe.jsb and miframe.jsb2 are also provided for further customization to suite
 deployment needs.  Either version creates a default miframe.js and miframe-debug.js for general 
 use as described above.
 
 
  
 