<html>
<%@page contentType="text/html"%>
<%@page errorPage="/errorpage.jsp" import="RegularExpression.RE, se.anatom.ejbca.webdist.webconfiguration.EjbcaWebBean,se.anatom.ejbca.ra.GlobalConfiguration
               ,se.anatom.ejbca.webdist.rainterface.RAInterfaceBean, se.anatom.ejbca.ra.raadmin.Profile, se.anatom.ejbca.webdist.rainterface.ProfileDataHandler, se.anatom.ejbca.ra.raadmin.ProfileExistsException"%>

<jsp:useBean id="ejbcawebbean" scope="session" class="se.anatom.ejbca.webdist.webconfiguration.EjbcaWebBean" />
<jsp:setProperty name="ejbcawebbean" property="*" /> 
<jsp:useBean id="ejbcarabean" scope="session" class="se.anatom.ejbca.webdist.rainterface.RAInterfaceBean" />
<jsp:setProperty name="ejbcarabean" property="*" /> 

<%! // Declarations 
  static final String ACTION                   = "action";
  static final String ACTION_EDIT_PROFILES     = "editprofiles";
  static final String ACTION_EDIT_PROFILE      = "editprofile";

  static final String CHECKBOX_VALUE           = Profile.TRUE;

//  Used in profiles.jsp
  static final String BUTTON_EDIT_PROFILE      = "buttoneditprofile"; 
  static final String BUTTON_DELETE_PROFILE    = "buttondeleteprofile";
  static final String BUTTON_ADD_PROFILE       = "buttonaddprofile"; 
  static final String BUTTON_RENAME_PROFILE    = "buttonrenameprofile";
  static final String BUTTON_CLONE_PROFILE     = "buttoncloneprofile";

  static final String SELECT_PROFILE           = "selectprofile";
  static final String TEXTFIELD_PROFILENAME    = "textfieldprofilename";
  static final String HIDDEN_PROFILENAME       = "hiddenprofilename";
 
// Buttons used in profile.jsp
  static final String BUTTON_SAVE              = "buttonsave";
  static final String BUTTON_CANCEL            = "buttoncancel";
 
  static final String TEXTFIELD_USERNAME         = "textfieldusername";
  static final String TEXTFIELD_PASSWORD         = "textfieldpassword";
  static final String TEXTFIELD_COMMONNAME       = "textfieldcommonname";
  static final String TEXTFIELD_ORGANIZATIONUNIT = "textfieldorganizationunit";
  static final String TEXTFIELD_ORGANIZATION     = "textfieldorganization";
  static final String TEXTFIELD_LOCALE           = "textfieldlocale";
  static final String TEXTFIELD_STATE            = "textfieldstate";
  static final String TEXTFIELD_COUNTRY          = "textfieldcountry";
  static final String TEXTFIELD_EMAIL            = "textfieldemail";

  static final String CHECKBOX_CLEARTEXTPASSWORD          = "checkboxcleartextpassword";
  static final String CHECKBOX_TYPEENDUSER                = "checkboxtypeenduser";
  static final String CHECKBOX_TYPERA                     = "checkboxtypera";
  static final String CHECKBOX_TYPERAADMIN                = "checkboxtyperaadmin";
  static final String CHECKBOX_TYPECA                     = "checkboxtypeca";
  static final String CHECKBOX_TYPECAADMIN                = "checkboxtypecaadmin";
  static final String CHECKBOX_TYPEROOTCA                 = "checkboxtyperootca";

  static final String CHECKBOX_REQUIRED_USERNAME          = "checkboxrequiredusername";
  static final String CHECKBOX_REQUIRED_PASSWORD          = "checkboxrequiredpassword";
  static final String CHECKBOX_REQUIRED_CLEARTEXTPASSWORD = "checkboxrequiredcleartextpassword";
  static final String CHECKBOX_REQUIRED_COMMONNAME        = "checkboxrequiredcommonname";
  static final String CHECKBOX_REQUIRED_ORGANIZATIONUNIT  = "checkboxrequiredorganizationunit";
  static final String CHECKBOX_REQUIRED_ORGANIZATION      = "checkboxrequiredorganization";
  static final String CHECKBOX_REQUIRED_LOCALE            = "checkboxrequiredlocale";
  static final String CHECKBOX_REQUIRED_STATE             = "checkboxrequiredstate";
  static final String CHECKBOX_REQUIRED_COUNTRY           = "checkboxrequiredcountry";
  static final String CHECKBOX_REQUIRED_EMAIL             = "checkboxrequiredemail";
  static final String CHECKBOX_REQUIRED_TYPEENDUSER       = "checkboxrequiredtypeenduser";
  static final String CHECKBOX_REQUIRED_TYPERA            = "checkboxrequiredtypera";
  static final String CHECKBOX_REQUIRED_TYPERAADMIN       = "checkboxrequiredtyperaadmin";
  static final String CHECKBOX_REQUIRED_TYPECA            = "checkboxrequiredtypeca";
  static final String CHECKBOX_REQUIRED_TYPECAADMIN       = "checkboxrequiredtypecaadmin";
  static final String CHECKBOX_REQUIRED_TYPEROOTCA        = "checkboxrequiredtyperootca";

  static final String CHECKBOX_CHANGEABLE_USERNAME          = "checkboxchangeableusername";
  static final String CHECKBOX_CHANGEABLE_PASSWORD          = "checkboxchangeablepassword";
  static final String CHECKBOX_CHANGEABLE_COMMONNAME        = "checkboxchangeablecommonname";
  static final String CHECKBOX_CHANGEABLE_ORGANIZATIONUNIT  = "checkboxchangeableorganizationunit";
  static final String CHECKBOX_CHANGEABLE_ORGANIZATION      = "checkboxchangeableorganization";
  static final String CHECKBOX_CHANGEABLE_LOCALE            = "checkboxchangeablelocale";
  static final String CHECKBOX_CHANGEABLE_STATE             = "checkboxchangeablestate";
  static final String CHECKBOX_CHANGEABLE_COUNTRY           = "checkboxchangeablecountry";
  static final String CHECKBOX_CHANGEABLE_EMAIL             = "checkboxchangeableemail";

  static final String CHECKBOX_USE_CLEARTEXTPASSWORD        = "checkboxusecleartextpassword";
  static final String CHECKBOX_USE_COMMONNAME               = "checkboxusecommonname";
  static final String CHECKBOX_USE_ORGANIZATIONUNIT         = "checkboxuseorganizationunit";
  static final String CHECKBOX_USE_ORGANIZATION             = "checkboxuseorganization";
  static final String CHECKBOX_USE_LOCALE                   = "checkboxuselocale";
  static final String CHECKBOX_USE_STATE                    = "checkboxusestate";
  static final String CHECKBOX_USE_COUNTRY                  = "checkboxusecountry";
  static final String CHECKBOX_USE_EMAIL                    = "checkboxuseemail";
  static final String CHECKBOX_USE_TYPEENDUSER              = "checkboxusetypeenduser";
  static final String CHECKBOX_USE_TYPERA                   = "checkboxusetypera";
  static final String CHECKBOX_USE_TYPERAADMIN              = "checkboxusetyperaadmin";
  static final String CHECKBOX_USE_TYPECA                   = "checkboxusetypeca";
  static final String CHECKBOX_USE_TYPECAADMIN              = "checkboxusetypecaadmin";
  static final String CHECKBOX_USE_TYPEROOTCA               = "checkboxusetyperootca";

  static final String SELECT_DEFAULTCERTTYPE                = "selectdefaultcerttype";
  static final String SELECT_AVAILABLECERTTYPES             = "selectavailablecerttypes";

  static final String SELECT_TYPE                         = "selecttype";
  String profile = null;
  // Declare Language file.

%>
<% 

  // Initialize environment
  String includefile = null;
  boolean  triedtoeditemptyprofile   = false;
  boolean  triedtodeleteemptyprofile = false;
  boolean  profileexists             = false;
  boolean  profiledeletefailed       = false;

  GlobalConfiguration globalconfiguration = ejbcawebbean.initialize(request); 
                                            ejbcarabean.initialize(request);
  String THIS_FILENAME            =  globalconfiguration .getRaPath()  + "/editprofiles/editprofiles.jsp";
%>
 
<head>
  <title><%= globalconfiguration .getEjbcaTitle() %></title>
  <base href="<%= ejbcawebbean.getBaseUrl() %>">
  <link rel=STYLESHEET href="<%= ejbcawebbean.getCssFile() %>">
  <script language=javascript src="<%= globalconfiguration .getRaAdminPath() %>ejbcajslib.js"></script>
</head>
<body>

<%  // Determine action 
  if( request.getParameter(ACTION) != null){
    if( request.getParameter(ACTION).equals(ACTION_EDIT_PROFILES)){
      if( request.getParameter(BUTTON_EDIT_PROFILE) != null){
          // Display  profilepage.jsp
         profile = request.getParameter(SELECT_PROFILE);
         if(profile != null){
           if(!profile.trim().equals("")){
             if(!profile.equals(ProfileDataHandler.EMPTY_PROFILE)){ 
               includefile="profilepage.jsp"; 
             }else{
                triedtoeditemptyprofile=true;
                profile= null;
             }
           } 
           else{ 
            profile= null;
          } 
        }
        if(profile == null){   
          includefile="profilespage.jsp";     
        }
      }
      if( request.getParameter(BUTTON_DELETE_PROFILE) != null) {
          // Delete profile and display profilespage. 
          profile = request.getParameter(SELECT_PROFILE);
          if(profile != null){
            if(!profile.trim().equals("")){
              if(!profile.equals(ProfileDataHandler.EMPTY_PROFILE)){ 
                profiledeletefailed = !ejbcarabean.removeProfile(profile);
              }else{
                triedtodeleteemptyprofile=true;
              }
            }
          }
          includefile="profilespage.jsp";             
      }
      if( request.getParameter(BUTTON_RENAME_PROFILE) != null){ 
         // Rename selected profile and display profilespage.
       String newprofilename = request.getParameter(TEXTFIELD_PROFILENAME);
       String oldprofilename = request.getParameter(SELECT_PROFILE);
       if(oldprofilename != null && newprofilename != null){
         if(!newprofilename.trim().equals("") && !oldprofilename.trim().equals("")){
           if(!oldprofilename.equals(ProfileDataHandler.EMPTY_PROFILE)){ 
             try{
               ejbcarabean.renameProfile(oldprofilename.trim(),newprofilename.trim());
             }catch( ProfileExistsException e){
               profileexists=true;
             }
           }else{
              triedtoeditemptyprofile=true;
           }        
         }
       }      
       includefile="profilespage.jsp"; 
      }
      if( request.getParameter(BUTTON_ADD_PROFILE) != null){
         // Add profile and display profilespage.
         profile = request.getParameter(TEXTFIELD_PROFILENAME);
         if(profile != null){
           if(!profile.trim().equals("")){
             try{
               ejbcarabean.addProfile(profile.trim());
             }catch( ProfileExistsException e){
               profileexists=true;
             }
           }      
         }
         includefile="profilespage.jsp"; 
      }
      if( request.getParameter(BUTTON_CLONE_PROFILE) != null){
         // clone profile and display profilespage.
       String newprofilename = request.getParameter(TEXTFIELD_PROFILENAME);
       String oldprofilename = request.getParameter(SELECT_PROFILE);
       if(oldprofilename != null && newprofilename != null){
         if(!newprofilename.trim().equals("") && !oldprofilename.trim().equals("")){
             try{ 
               ejbcarabean.cloneProfile(oldprofilename.trim(),newprofilename.trim());
             }catch( ProfileExistsException e){
               profileexists=true;
             }
         }
       }      
          includefile="profilespage.jsp"; 
      }
    }
    if( request.getParameter(ACTION).equals(ACTION_EDIT_PROFILE)){
         // Display edit access rules page.
       profile = request.getParameter(HIDDEN_PROFILENAME);
       if(profile != null){
         if(!profile.trim().equals("")){
           if(request.getParameter(BUTTON_SAVE) != null){
             Profile profiledata = ejbcarabean.getProfile(profile);
             // Save changes.
             profiledata.setValue(Profile.USERNAME ,request.getParameter(TEXTFIELD_USERNAME));
             profiledata.setRequired(Profile.USERNAME ,request.getParameter(CHECKBOX_REQUIRED_USERNAME));
             profiledata.setChangeable(Profile.USERNAME ,request.getParameter(CHECKBOX_CHANGEABLE_USERNAME));

             profiledata.setValue(Profile.PASSWORD ,request.getParameter(TEXTFIELD_PASSWORD));
             profiledata.setRequired(Profile.PASSWORD ,request.getParameter(CHECKBOX_REQUIRED_PASSWORD));
             profiledata.setChangeable(Profile.PASSWORD ,request.getParameter(CHECKBOX_CHANGEABLE_PASSWORD));
 
             profiledata.setValue(Profile.CLEARTEXTPASSWORD ,request.getParameter(CHECKBOX_CLEARTEXTPASSWORD));
             profiledata.setRequired(Profile.CLEARTEXTPASSWORD ,request.getParameter(CHECKBOX_REQUIRED_CLEARTEXTPASSWORD)); 
             profiledata.setUse(Profile.CLEARTEXTPASSWORD ,request.getParameter(CHECKBOX_USE_CLEARTEXTPASSWORD)); 

             profiledata.setValue(Profile.COMMONNAME ,request.getParameter(TEXTFIELD_COMMONNAME));
             profiledata.setRequired(Profile.COMMONNAME ,request.getParameter(CHECKBOX_REQUIRED_COMMONNAME));
             profiledata.setChangeable(Profile.COMMONNAME ,request.getParameter(CHECKBOX_CHANGEABLE_COMMONNAME));
             profiledata.setUse(Profile.COMMONNAME ,request.getParameter(CHECKBOX_USE_COMMONNAME)); 

             profiledata.setValue(Profile.ORGANIZATIONUNIT ,request.getParameter(TEXTFIELD_ORGANIZATIONUNIT));
             profiledata.setRequired(Profile.ORGANIZATIONUNIT ,request.getParameter(CHECKBOX_REQUIRED_ORGANIZATIONUNIT));
             profiledata.setChangeable(Profile.ORGANIZATIONUNIT ,request.getParameter(CHECKBOX_CHANGEABLE_ORGANIZATIONUNIT));
             profiledata.setUse(Profile.ORGANIZATIONUNIT ,request.getParameter(CHECKBOX_USE_ORGANIZATIONUNIT)); 

             profiledata.setValue(Profile.ORGANIZATION ,request.getParameter(TEXTFIELD_ORGANIZATION));
             profiledata.setRequired(Profile.ORGANIZATION ,request.getParameter(CHECKBOX_REQUIRED_ORGANIZATION));
             profiledata.setChangeable(Profile.ORGANIZATION ,request.getParameter(CHECKBOX_CHANGEABLE_ORGANIZATION));
             profiledata.setUse(Profile.ORGANIZATION ,request.getParameter(CHECKBOX_USE_ORGANIZATION)); 

             profiledata.setValue(Profile.LOCALE ,request.getParameter(TEXTFIELD_LOCALE));
             profiledata.setRequired(Profile.LOCALE ,request.getParameter(CHECKBOX_REQUIRED_LOCALE));
             profiledata.setChangeable(Profile.LOCALE ,request.getParameter(CHECKBOX_CHANGEABLE_LOCALE)); 
             profiledata.setUse(Profile.LOCALE ,request.getParameter(CHECKBOX_USE_LOCALE)); 

             profiledata.setValue(Profile.STATE ,request.getParameter(TEXTFIELD_STATE));
             profiledata.setRequired(Profile.STATE ,request.getParameter(CHECKBOX_REQUIRED_STATE));
             profiledata.setChangeable(Profile.STATE ,request.getParameter(CHECKBOX_CHANGEABLE_STATE)); 
             profiledata.setUse(Profile.STATE ,request.getParameter(CHECKBOX_USE_STATE)); 
             
             String countrystring = request.getParameter(TEXTFIELD_COUNTRY);
             if(countrystring!= null)
               profiledata.setValue(Profile.COUNTRY  ,countrystring.toUpperCase());
             else  
               profiledata.setValue(Profile.COUNTRY  ,null);
             profiledata.setRequired(Profile.COUNTRY  ,request.getParameter(CHECKBOX_REQUIRED_COUNTRY));
             profiledata.setChangeable(Profile.COUNTRY  ,request.getParameter(CHECKBOX_CHANGEABLE_COUNTRY)); 
             profiledata.setUse(Profile.COUNTRY ,request.getParameter(CHECKBOX_USE_COUNTRY)); 

             profiledata.setValue(Profile.EMAIL ,request.getParameter(TEXTFIELD_EMAIL));
             profiledata.setRequired(Profile.EMAIL ,request.getParameter(CHECKBOX_REQUIRED_EMAIL));
             profiledata.setChangeable(Profile.EMAIL ,request.getParameter(CHECKBOX_CHANGEABLE_EMAIL)); 
             profiledata.setUse(Profile.EMAIL ,request.getParameter(CHECKBOX_USE_EMAIL)); 
 
             profiledata.setValue(Profile.TYPE_ENDUSER ,request.getParameter(CHECKBOX_TYPEENDUSER));
             profiledata.setRequired(Profile.TYPE_ENDUSER ,request.getParameter(CHECKBOX_REQUIRED_TYPEENDUSER));
             profiledata.setUse(Profile.TYPE_ENDUSER ,request.getParameter(CHECKBOX_USE_TYPEENDUSER)); 

             profiledata.setValue(Profile.TYPE_RA ,request.getParameter(CHECKBOX_TYPERA));
             profiledata.setRequired(Profile.TYPE_RA ,request.getParameter(CHECKBOX_REQUIRED_TYPERA));
             profiledata.setUse(Profile.TYPE_RA ,request.getParameter(CHECKBOX_USE_TYPERA)); 

             profiledata.setValue(Profile.TYPE_RAADMIN ,request.getParameter(CHECKBOX_TYPERAADMIN));
             profiledata.setRequired(Profile.TYPE_RAADMIN ,request.getParameter(CHECKBOX_REQUIRED_TYPERAADMIN));
             profiledata.setUse(Profile.TYPE_RAADMIN ,request.getParameter(CHECKBOX_USE_TYPERAADMIN)); 

             profiledata.setValue(Profile.TYPE_CA ,request.getParameter(CHECKBOX_TYPECA));
             profiledata.setRequired(Profile.TYPE_CA ,request.getParameter(CHECKBOX_REQUIRED_TYPECA));
             profiledata.setUse(Profile.TYPE_CA ,request.getParameter(CHECKBOX_USE_TYPECA)); 

             profiledata.setValue(Profile.TYPE_CAADMIN ,request.getParameter(CHECKBOX_TYPECAADMIN));
             profiledata.setRequired(Profile.TYPE_CAADMIN ,request.getParameter(CHECKBOX_REQUIRED_TYPECAADMIN));
             profiledata.setUse(Profile.TYPE_CAADMIN ,request.getParameter(CHECKBOX_USE_TYPECAADMIN)); 

             profiledata.setValue(Profile.TYPE_ROOTCA ,request.getParameter(CHECKBOX_TYPEROOTCA));
             profiledata.setRequired(Profile.TYPE_ROOTCA ,request.getParameter(CHECKBOX_REQUIRED_TYPEROOTCA));
             profiledata.setUse(Profile.TYPE_ROOTCA ,request.getParameter(CHECKBOX_USE_TYPEROOTCA)); 
 
             String defaultcerttype =  request.getParameter(SELECT_DEFAULTCERTTYPE);
             profiledata.setValue(Profile.DEFAULTCERTTYPE, defaultcerttype);
             profiledata.setRequired(Profile.DEFAULTCERTTYPE,Profile.TRUE);

             String[] values = request.getParameterValues(SELECT_AVAILABLECERTTYPES);
 
             if(defaultcerttype != null){
               String availablecert =defaultcerttype;
               if(values!= null){
                 for(int i=0; i< values.length; i++){
                     if(!values[i].equals(defaultcerttype))
                       availablecert += Profile.SPLITCHAR + values[i];                      
                 }
               } 
               profiledata.setValue(Profile.AVAILABLECERTTYPES, availablecert);
               profiledata.setRequired(Profile.AVAILABLECERTTYPES,Profile.TRUE);    
             }
 
             
          
             ejbcarabean.changeProfile(profile,profiledata);
           }
           if(request.getParameter(BUTTON_CANCEL) != null){
              // Don't save changes.
           }
             includefile="profilespage.jsp";
         }
      }
    }
  }
  else{ 
    // Display main user group editing page. 
          includefile="profilespage.jsp"; 

  }
 // Include page
  if( includefile.equals("profilepage.jsp")){ %>
   <%@ include file="profilepage.jsp" %>
<%}
  if( includefile.equals("profilespage.jsp")){ %>
   <%@ include file="profilespage.jsp" %> 
<%}

   // Include Footer 
   String footurl =   globalconfiguration.getFootBanner(); %>
   
  <jsp:include page="<%= footurl %>" />

</body>
</html>
