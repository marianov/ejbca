/*************************************************************************
 *                                                                       *
 *  EJBCA: The OpenSource Certificate Authority                          *
 *                                                                       *
 *  This software is free software; you can redistribute it and/or       *
 *  modify it under the terms of the GNU Lesser General Public           *
 *  License as published by the Free Software Foundation; either         *
 *  version 2.1 of the License, or any later version.                    *
 *                                                                       *
 *  See terms of license at gnu.org.                                     *
 *                                                                       *
 *************************************************************************/

package org.ejbca.core.ejb.ra.raadmin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;

import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.log4j.Logger;
import org.ejbca.core.ejb.authorization.AuthorizationSessionLocal;
import org.ejbca.core.ejb.ca.caadmin.CAAdminSessionLocal;
import org.ejbca.core.ejb.log.LogSessionLocal;
import org.ejbca.core.model.InternalResources;
import org.ejbca.core.model.SecConst;
import org.ejbca.core.model.authorization.AuthorizationDeniedException;
import org.ejbca.core.model.log.Admin;
import org.ejbca.core.model.log.LogConstants;
import org.ejbca.core.model.ra.raadmin.AdminPreference;
import org.ejbca.core.model.ra.raadmin.EndEntityProfile;
import org.ejbca.core.model.ra.raadmin.EndEntityProfileExistsException;
import org.ejbca.core.model.ra.raadmin.GlobalConfiguration;

/**
 * Stores data used by web server clients.
 * Uses JNDI name for datasource as defined in env 'Datasource' in ejb-jar.xml.
 *
 * @version $Id$
 *
 * @ejb.bean description="Session bean handling core CA function,signing certificates"
 *   display-name="RaAdminSB"
 *   name="RaAdminSession"
 *   jndi-name="RaAdminSession"
 *   local-jndi-name="RaAdminSessionLocal"
 *   view-type="both"
 *   type="Stateless"
 *   transaction-type="Container"
 *
 * @ejb.transaction type="Required"
 *
 * @weblogic.enable-call-by-reference True
 *
 * @ejb.home
 *   extends="javax.ejb.EJBHome"
 *   remote-class="org.ejbca.core.ejb.ra.raadmin.IRaAdminSessionHome"
 *   local-extends="javax.ejb.EJBLocalHome"
 *   local-class="org.ejbca.core.ejb.ra.raadmin.IRaAdminSessionLocalHome"
 *
 * @ejb.interface
 *   extends="javax.ejb.EJBObject"
 *   remote-class="org.ejbca.core.ejb.ra.raadmin.IRaAdminSessionRemote"
 *   local-extends="javax.ejb.EJBLocalObject"
 *   local-class="org.ejbca.core.ejb.ra.raadmin.IRaAdminSessionLocal"
 *
 * @ejb.ejb-external-ref description="The log session bean"
 *   view-type="local"
 *   ref-name="ejb/LogSessionLocal"
 *   type="Session"
 *   home="org.ejbca.core.ejb.log.ILogSessionLocalHome"
 *   business="org.ejbca.core.ejb.log.ILogSessionLocal"
 *   link="LogSession"
 *
 * @ejb.ejb-external-ref description="The Authorization session bean"
 *   view-type="local"
 *   ref-name="ejb/AuthorizationSessionLocal"
 *   type="Session"
 *   home="org.ejbca.core.ejb.authorization.IAuthorizationSessionLocalHome"
 *   business="org.ejbca.core.ejb.authorization.IAuthorizationSessionLocal"
 *   link="AuthorizationSession"
 *
 * @ejb.ejb-external-ref description="The AdminPreferencesData Entity bean"
 *   view-type="local"
 *   ref-name="ejb/AdminPreferencesDataLocal"
 *   type="Entity"
 *   home="org.ejbca.core.ejb.ra.raadmin.AdminPreferencesDataLocalHome"
 *   business="org.ejbca.core.ejb.ra.raadmin.AdminPreferencesDataLocal"
 *   link="AdminPreferencesData"
 *
 * @ejb.ejb-external-ref description="The EndEntityProfileData Entity bean"
 *   view-type="local"
 *   ref-name="ejb/EndEntityProfileDataLocal"
 *   type="Entity"
 *   home="org.ejbca.core.ejb.ra.raadmin.EndEntityProfileDataLocalHome"
 *   business="org.ejbca.core.ejb.ra.raadmin.EndEntityProfileDataLocal"
 *   link="EndEntityProfileData"
 *
 * @ejb.ejb-external-ref description="The GlobalConfigurationData Entity bean"
 *   view-type="local"
 *   ref-name="ejb/GlobalConfigurationDataLocal"
 *   type="Entity"
 *   home="org.ejbca.core.ejb.ra.raadmin.GlobalConfigurationDataLocalHome"
 *   business="org.ejbca.core.ejb.ra.raadmin.GlobalConfigurationDataLocal"
 *   link="GlobalConfigurationData"
 *
 * @ejb.ejb-external-ref description="The CAAdmin Session Bean"
 *   view-type="local"
 *   ref-name="ejb/CAAdminSessionLocal"
 *   type="Session"
 *   home="org.ejbca.core.ejb.ca.caadmin.ICAAdminSessionLocalHome"
 *   business="org.ejbca.core.ejb.ca.caadmin.ICAAdminSessionLocal"
 *   link="CAAdminSession"
 *
 * @jboss.method-attributes
 *   pattern = "get*"
 *   read-only = "true"
 *
 * @jboss.method-attributes
 *   pattern = "is*"
 *   read-only = "true"
 *   
 * @jboss.method-attributes
 *   pattern = "exists*"
 *   read-only = "true"
 *
 */
@Stateless(mappedName = org.ejbca.core.ejb.JndiHelper.APP_JNDI_PREFIX + "RaAdminSessionRemote")
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class LocalRaAdminSessionBean implements RaAdminSessionLocal, RaAdminSessionRemote {

	private static final Logger log = Logger.getLogger(LocalRaAdminSessionBean.class);
    /** Internal localization of logs and errors */
    private static final InternalResources intres = InternalResources.getInstance();

    /** Cache variable containing the global configuration. */
    private GlobalConfiguration globalconfiguration = null;
    /** Constant indicating minimum time between updates of the global configuration cache. In milliseconds, 30 seconds. */
    private static final long MIN_TIME_BETWEEN_GLOBCONF_UPDATES = 30000;
    /** help variable used to control that update isn't performed to often. */
    private long lastupdatetime = -1;

    @PersistenceContext(unitName="ejbca")
    private EntityManager entityManager;

    @EJB
    private LogSessionLocal logSession;
    @EJB
    private CAAdminSessionLocal caAdminSession;
    @EJB
    private AuthorizationSessionLocal authorizationSession;

    public static final String EMPTY_ENDENTITYPROFILENAME   = "EMPTY";
    private static final String DEFAULTUSERPREFERENCE = "default";
    public static final String EMPTY_ENDENTITYPROFILE = EMPTY_ENDENTITYPROFILENAME;
    public static final int EMPTY_ENDENTITYPROFILEID  = SecConst.EMPTY_ENDENTITYPROFILE;

     /**
     * Finds the admin preference belonging to a certificate serialnumber (??). Returns null if admin doesn't exists.
     * @ejb.interface-method
     */
    public AdminPreference getAdminPreference(Admin admin, String certificatefingerprint){
    	if (log.isTraceEnabled()) {
    		log.trace(">getAdminPreference()");
    	}
        AdminPreference ret = null;
        AdminPreferencesData apdata = AdminPreferencesData.findById(entityManager, certificatefingerprint);
        if (apdata != null) {
            ret = apdata.getAdminPreference();
        }
    	if (log.isTraceEnabled()) {
    		log.trace("<getAdminPreference()");
    	}
        return ret;
    }

    /**
     * Adds a admin preference to the database. Returns false if admin already exists.
     * @ejb.interface-method
     */
    public boolean addAdminPreference(Admin admin, String certificatefingerprint, AdminPreference adminpreference){
    	if (log.isTraceEnabled()) {
        	log.trace(">addAdminPreference(fingerprint : " + certificatefingerprint + ")");
    	}
    	boolean ret = false;
    	boolean exists = false;
    	// EJB 2.1 only?: We must actually check if there is one before we try to add it, because wls does not allow us to catch any errors if creating fails, that sux
    	if (AdminPreferencesData.findById(entityManager, certificatefingerprint) == null) {
    		try {
    			AdminPreferencesData apdata = new AdminPreferencesData(certificatefingerprint, adminpreference);
    			entityManager.persist(apdata);
    			String msg = intres.getLocalizedMessage("ra.adminprefadded", apdata.getId());            	
    			logSession.log(admin, admin.getCaId(), LogConstants.MODULE_RA, new java.util.Date(),null, null, LogConstants.EVENT_INFO_ADMINISTRATORPREFERENCECHANGED,msg);
    			ret = true;        		
    		} catch (Exception e) {
    			String msg = intres.getLocalizedMessage("ra.adminprefexists");            	
    			logSession.log(admin, admin.getCaId(), LogConstants.MODULE_RA, new java.util.Date(),null, null, LogConstants.EVENT_INFO_ADMINISTRATORPREFERENCECHANGED,msg);
    		}
    	} else {
    		String msg = intres.getLocalizedMessage("ra.adminprefexists");            	
    		logSession.log(admin, admin.getCaId(), LogConstants.MODULE_RA, new java.util.Date(),null, null, LogConstants.EVENT_INFO_ADMINISTRATORPREFERENCECHANGED,msg);            	        		
    	}
		log.trace("<addAdminPreference()");
    	return ret;
    }

    /**
     * Changes the admin preference in the database. Returns false if admin doesn't exists.
     * @ejb.interface-method
     */
    public boolean changeAdminPreference(Admin admin, String certificatefingerprint, AdminPreference adminpreference){
    	if (log.isTraceEnabled()) {
    		log.trace(">changeAdminPreference(fingerprint : " + certificatefingerprint + ")");
    	}
    	return updateAdminPreference(admin, certificatefingerprint, adminpreference, true);
    }

    /**
     * Changes the admin preference in the database. Returns false if admin doesn't exists.
     * @ejb.interface-method
     */
    public boolean changeAdminPreferenceNoLog(Admin admin, String certificatefingerprint, AdminPreference adminpreference){
    	if (log.isTraceEnabled()) {
    		log.trace(">changeAdminPreferenceNoLog(fingerprint : " + certificatefingerprint + ")");
    	}
    	return updateAdminPreference(admin, certificatefingerprint, adminpreference, false);
    }

    /**
     * Checks if a admin preference exists in the database.
     * @ejb.interface-method
     * @ejb.transaction type="Supports"
     */
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public boolean existsAdminPreference(Admin admin, String certificatefingerprint){
    	if (log.isTraceEnabled()) {
    	    log.trace(">existsAdminPreference(fingerprint : " + certificatefingerprint + ")");
    	}
    	boolean ret = false;
    	AdminPreferencesData apdata = AdminPreferencesData.findById(entityManager, certificatefingerprint);
    	if (apdata != null) {
    		log.debug("Found admin preferences with id "+apdata.getId());
    		ret = true;
        }
		log.trace("<existsAdminPreference()");
        return ret;
    }

    /**
     * Function that returns the default admin preference.
     *
     * @throws EJBException if a communication or other error occurs.
     * @ejb.interface-method
     * @ejb.transaction type="Supports"
     */
    @TransactionAttribute(TransactionAttributeType.REQUIRED)	// We access an entity manager.. we must have be in a transaction!
    public AdminPreference getDefaultAdminPreference(Admin admin){
    	if (log.isTraceEnabled()) {
    		log.trace(">getDefaultAdminPreference()");
    	}
        AdminPreference ret = null;
        AdminPreferencesData apdata = AdminPreferencesData.findById(entityManager, DEFAULTUSERPREFERENCE);
        if (apdata != null) {
            ret = apdata.getAdminPreference();
        } else {
        	try{
        		// Create new configuration
        		AdminPreferencesData newapdata = new AdminPreferencesData(DEFAULTUSERPREFERENCE,new AdminPreference());
        		entityManager.persist(newapdata);
        		ret = newapdata.getAdminPreference();
        	}catch(Exception e){
        		throw new EJBException(e);
        	}
        }
    	if (log.isTraceEnabled()) {
    		log.trace("<getDefaultAdminPreference()");
    	}
        return ret;
    }

    /**
     * Function that saves the default admin preference.
     *
     * @throws EJBException if a communication or other error occurs.
     * @ejb.interface-method
     */
    public void saveDefaultAdminPreference(Admin admin, AdminPreference defaultadminpreference){
    	if (log.isTraceEnabled()) {
    		log.trace(">saveDefaultAdminPreference()");
    	}
    	AdminPreferencesData apdata = AdminPreferencesData.findById(entityManager, DEFAULTUSERPREFERENCE);
    	if (apdata != null) {
    		apdata.setAdminPreference(defaultadminpreference);
    		String msg = intres.getLocalizedMessage("ra.defaultadminprefsaved");            	
    		logSession.log(admin, admin.getCaId(), LogConstants.MODULE_RA, new java.util.Date(),null, null, LogConstants.EVENT_INFO_ADMINISTRATORPREFERENCECHANGED,msg);
    	} else {
    		String msg = intres.getLocalizedMessage("ra.errorsavedefaultadminpref");            	
    		logSession.log(admin, admin.getCaId(), LogConstants.MODULE_RA, new java.util.Date(),null, null, LogConstants.EVENT_ERROR_ADMINISTRATORPREFERENCECHANGED,msg);
    		throw new EJBException(msg);
    	}
    	if (log.isTraceEnabled()) {
    		log.trace("<saveDefaultAdminPreference()");
    	}
    }

    /**
     * A method designed to be called at startuptime to (possibly) upgrade end entity profiles.
     * This method will read all End Entity Profiles and as a side-effect upgrade them if the version if changed for upgrade.
     * Can have a side-effect of upgrading a profile, therefore the Required transaction setting.
     * 
     * @param admin administrator calling the method
     * 
     * @ejb.transaction type="Required"
     * @ejb.interface-method
     */
    public void initializeAndUpgradeProfiles(Admin admin) {
    	Collection<EndEntityProfileData> result = EndEntityProfileData.findAll(entityManager);
    	Iterator<EndEntityProfileData> iter = result.iterator();
    	while(iter.hasNext()){
    		EndEntityProfileData pdata = iter.next();
    		String name = pdata.getProfileName();
    		pdata.upgradeProfile();
    		if (log.isDebugEnabled()) {
    			log.debug("Loaded end entity profile: "+name);
    		}
    	}
    }
    
    /**
     * Adds a profile to the database.
     *
     * @param admin administrator performing task
     * @param profilename readable profile name
     * @param profile profile to be added
     * @ejb.interface-method
     *
     */
    public void addEndEntityProfile(Admin admin, String profilename, EndEntityProfile profile) throws EndEntityProfileExistsException {
    	addEndEntityProfile(admin,findFreeEndEntityProfileId(),profilename,profile);
    }

    /**
     * Adds a profile to the database.
     *
     * @param admin administrator performing task
     * @param profileid internal ID of new profile, use only if you know it's right.
     * @param profilename readable profile name
     * @param profile profile to be added
     * @ejb.interface-method
     *
     */
    public void addEndEntityProfile(Admin admin, int profileid, String profilename, EndEntityProfile profile) throws EndEntityProfileExistsException{
    	if(profilename.trim().equalsIgnoreCase(EMPTY_ENDENTITYPROFILENAME)){
			String msg = intres.getLocalizedMessage("ra.erroraddprofile", profilename);            	
			logSession.log(admin, admin.getCaId(), LogConstants.MODULE_RA,  new java.util.Date(),null, null, LogConstants.EVENT_ERROR_ENDENTITYPROFILE,msg);
			throw new EndEntityProfileExistsException();
		}
		if (isFreeEndEntityProfileId(profileid) == false) {
			String msg = intres.getLocalizedMessage("ra.erroraddprofile", profilename);            	
			logSession.log(admin, admin.getCaId(), LogConstants.MODULE_RA,  new java.util.Date(),null, null, LogConstants.EVENT_ERROR_ENDENTITYPROFILE,msg);
			throw new EndEntityProfileExistsException();
		}
		if (EndEntityProfileData.findByProfileName(entityManager, profilename) != null) {
			String msg = intres.getLocalizedMessage("ra.erroraddprofile", profilename);            	
			logSession.log(admin, admin.getCaId(), LogConstants.MODULE_RA,  new java.util.Date(),null, null, LogConstants.EVENT_ERROR_ENDENTITYPROFILE,msg);
			throw new EndEntityProfileExistsException();
		} else {
			try {
				entityManager.persist(new EndEntityProfileData(new Integer(profileid), profilename, profile));
				String msg = intres.getLocalizedMessage("ra.addedprofile", profilename);            	
				logSession.log(admin, admin.getCaId(), LogConstants.MODULE_RA, new java.util.Date(), null, null,
						LogConstants.EVENT_INFO_ENDENTITYPROFILE,msg);
			} catch (Exception e) {
				String msg = intres.getLocalizedMessage("ra.erroraddprofile", profilename);            	
				log.error(msg, e);
				logSession.log(admin, admin.getCaId(), LogConstants.MODULE_RA, new java.util.Date(), null, null,
						LogConstants.EVENT_ERROR_ENDENTITYPROFILE,msg);
			}
		 }
	 }

    /**
     * Adds a end entity profile to a group with the same content as the original profile.
     * @ejb.interface-method
     */
    public void cloneEndEntityProfile(Admin admin, String originalprofilename, String newprofilename) throws EndEntityProfileExistsException{
    	if(newprofilename.trim().equalsIgnoreCase(EMPTY_ENDENTITYPROFILENAME)){
    		String msg = intres.getLocalizedMessage("ra.errorcloneprofile", newprofilename, originalprofilename);            	
    		logSession.log(admin, admin.getCaId(), LogConstants.MODULE_RA,  new java.util.Date(),null, null, LogConstants.EVENT_ERROR_ENDENTITYPROFILE,msg);
    		throw new EndEntityProfileExistsException();
    	}
    	if (EndEntityProfileData.findByProfileName(entityManager, newprofilename) == null) {
    		EndEntityProfileData pdl = EndEntityProfileData.findByProfileName(entityManager, originalprofilename);
    		boolean success = false;
    		if (pdl != null) {
    			try {
    				entityManager.persist(new EndEntityProfileData(new Integer(findFreeEndEntityProfileId()),newprofilename, (EndEntityProfile) pdl.getProfile().clone()));
    				String msg = intres.getLocalizedMessage("ra.clonedprofile", newprofilename, originalprofilename);            	
    				logSession.log(admin, admin.getCaId(), LogConstants.MODULE_RA, new java.util.Date(),null, null, LogConstants.EVENT_INFO_ENDENTITYPROFILE,msg);
    				success = true;
    			} catch (Exception e) {
    			}
    		}
    		if (!success) {
    			String msg = intres.getLocalizedMessage("ra.errorcloneprofile", newprofilename, originalprofilename);            	
    			logSession.log(admin, admin.getCaId(), LogConstants.MODULE_RA,  new java.util.Date(),null, null, LogConstants.EVENT_ERROR_ENDENTITYPROFILE,msg);
    		}
    	} else {
    		String msg = intres.getLocalizedMessage("ra.errorcloneprofile", newprofilename, originalprofilename);            	
    		logSession.log(admin, admin.getCaId(), LogConstants.MODULE_RA,  new java.util.Date(),null, null, LogConstants.EVENT_ERROR_ENDENTITYPROFILE,msg);
    		throw new EndEntityProfileExistsException();
    	}
    }

    /**
     * Removes an end entity profile from the database.
     * @ejb.interface-method
     */
    public void removeEndEntityProfile(Admin admin, String profilename) {
    	try{
    		EndEntityProfileData pdl = EndEntityProfileData.findByProfileName(entityManager, profilename);
    		entityManager.remove(pdl);
    		String msg = intres.getLocalizedMessage("ra.removedprofile", profilename);            	
    		logSession.log(admin, admin.getCaId(), LogConstants.MODULE_RA, new java.util.Date(),null, null, LogConstants.EVENT_INFO_ENDENTITYPROFILE,msg);
    	}catch(Exception e){
    		String msg = intres.getLocalizedMessage("ra.errorremoveprofile", profilename);            	
    		logSession.log(admin, admin.getCaId(), LogConstants.MODULE_RA, new java.util.Date(),null, null, LogConstants.EVENT_ERROR_ENDENTITYPROFILE,msg);
    	}
    }

    /**
     * Renames a end entity profile
     * @ejb.interface-method
     */
    public void renameEndEntityProfile(Admin admin, String oldprofilename, String newprofilename) throws EndEntityProfileExistsException{
    	if(newprofilename.trim().equalsIgnoreCase(EMPTY_ENDENTITYPROFILENAME) || oldprofilename.trim().equalsIgnoreCase(EMPTY_ENDENTITYPROFILENAME)){
    		String msg = intres.getLocalizedMessage("ra.errorrenameprofile", oldprofilename, newprofilename);            	
    		logSession.log(admin, admin.getCaId(), LogConstants.MODULE_RA, new java.util.Date(),null, null, LogConstants.EVENT_ERROR_ENDENTITYPROFILE,msg);
    		throw new EndEntityProfileExistsException();
    	}
    	if (EndEntityProfileData.findByProfileName(entityManager, newprofilename) != null) {
    		String msg = intres.getLocalizedMessage("ra.errorrenameprofile", oldprofilename, newprofilename);            	
    		logSession.log(admin, admin.getCaId(), LogConstants.MODULE_RA, new java.util.Date(),null, null, LogConstants.EVENT_ERROR_ENDENTITYPROFILE,msg);
    		throw new EndEntityProfileExistsException();
    	} else {
    		EndEntityProfileData pdl = EndEntityProfileData.findByProfileName(entityManager, oldprofilename);
    		if (pdl != null) {
    			pdl.setProfileName(newprofilename);
    			String msg = intres.getLocalizedMessage("ra.renamedprofile", oldprofilename, newprofilename);            	
    			logSession.log(admin, admin.getCaId(), LogConstants.MODULE_RA, new java.util.Date(),null, null, LogConstants.EVENT_INFO_ENDENTITYPROFILE,msg );
    		} else {
    			String msg = intres.getLocalizedMessage("ra.errorrenameprofile", oldprofilename, newprofilename);            	
    			logSession.log(admin, admin.getCaId(), LogConstants.MODULE_RA, new java.util.Date(),null, null, LogConstants.EVENT_ERROR_ENDENTITYPROFILE,msg );
    		}
    	}
    }

    /**
     * Updates profile data
     * @ejb.interface-method
     */
    public void changeEndEntityProfile(Admin admin, String profilename, EndEntityProfile profile){
    	EndEntityProfileData pdl = EndEntityProfileData.findByProfileName(entityManager, profilename);
    	if (pdl != null) {
            pdl.setProfile(profile);
			String msg = intres.getLocalizedMessage("ra.changedprofile", profilename);            	
            logSession.log(admin, admin.getCaId(), LogConstants.MODULE_RA, new java.util.Date(),null, null, LogConstants.EVENT_INFO_ENDENTITYPROFILE,msg);
    	} else {
			String msg = intres.getLocalizedMessage("ra.errorchangeprofile", profilename);            	
            logSession.log(admin, admin.getCaId(), LogConstants.MODULE_RA, new java.util.Date(),null, null, LogConstants.EVENT_ERROR_ENDENTITYPROFILE,msg);
        }
    }

    /**
     * Retrives a Collection of id:s (Integer) to authorized profiles.
     * @ejb.transaction type="Supports"
     * @ejb.interface-method
     */
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public Collection getAuthorizedEndEntityProfileIds(Admin admin){
      ArrayList<Integer> returnval = new ArrayList<Integer>();
      HashSet<Integer> authorizedcaids = new HashSet<Integer>(caAdminSession.getAvailableCAs(admin));
      //debug("Admin authorized to "+authorizedcaids.size()+" CAs.");
      try{
    	  if(authorizationSession.isAuthorizedNoLog(admin, "/super_administrator")) {
    		  returnval.add(new Integer(SecConst.EMPTY_ENDENTITYPROFILE));
    	  }
      }catch(AuthorizationDeniedException e){
      }
      try{
          Iterator<EndEntityProfileData> i = EndEntityProfileData.findAll(entityManager).iterator();
          while(i.hasNext()){
              EndEntityProfileData next = i.next();
              // Check if all profiles available CAs exists in authorizedcaids.
              String value = next.getProfile().getValue(EndEntityProfile.AVAILCAS, 0);
              //debug("AvailCAs: "+value);
              if (value != null) {
                  String[] availablecas = value.split(EndEntityProfile.SPLITCHAR);
                  //debug("No of available CAs: "+availablecas.length);
                  boolean allexists = true;
                  for(int j=0; j < availablecas.length; j++){
                      //debug("Available CA["+j+"]: "+availablecas[j]);
                      Integer caid = new Integer(availablecas[j]);
                      // If this is the special value ALLCAs we are authorized
                      if ( (caid.intValue() != SecConst.ALLCAS) && (!authorizedcaids.contains(caid)) ) {
                    	  allexists = false;
                    	  if (log.isDebugEnabled()) {
                    		  log.debug("Profile "+next.getId()+" not authorized");
                    	  }
                    	  break;
                      }
                  }
                  if(allexists) {
                      //debug("Adding "+next.getId());
                      returnval.add(next.getId());
                  }
              }
          }
      }catch(Exception e){
    	  String msg = intres.getLocalizedMessage("ra.errorgetids");    	  
          log.error(msg, e);
      }
      return returnval;
    }

    /**
     * Method creating a hashmap mapping profile id (Integer) to profile name (String).
     * @ejb.transaction type="Supports"
     * @ejb.interface-method
     */
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public HashMap getEndEntityProfileIdToNameMap(Admin admin){
    	if (log.isTraceEnabled()) {
    		log.trace(">getEndEntityProfileIdToNameMap");
    	}
    	HashMap<Integer,String> returnval = new HashMap<Integer,String>();
    	returnval.put(new Integer(SecConst.EMPTY_ENDENTITYPROFILE),EMPTY_ENDENTITYPROFILENAME);
    	try{
    		Collection<EndEntityProfileData> result = EndEntityProfileData.findAll(entityManager);
    		//debug("Found "+result.size()+ " end entity profiles.");
    		Iterator<EndEntityProfileData> i = result.iterator();
    		while(i.hasNext()){
    			EndEntityProfileData next = i.next();
    			//debug("Added "+next.getId()+ ", "+next.getProfileName());
    			returnval.put(next.getId(),next.getProfileName());
    		}
    	}catch(Exception e) {
    		String msg = intres.getLocalizedMessage("ra.errorreadprofiles");    	  
    		log.error(msg, e);
    	}
    	if (log.isTraceEnabled()) {
    		log.trace("<getEndEntityProfileIdToNameMap");
    	}
    	return returnval;
    }

    /**
     * Finds a end entity profile by id.
     * 
     * @return EndEntityProfile or null if it does not exist
     * 
     * @ejb.transaction type="Supports"
     * @ejb.interface-method
     */
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public EndEntityProfile getEndEntityProfile(Admin admin, int id){
    	if (log.isTraceEnabled()) {
            log.trace(">getEndEntityProfile("+id+")");    		
    	}
        EndEntityProfile returnval=null;
        if(id==SecConst.EMPTY_ENDENTITYPROFILE) {
        	returnval = new EndEntityProfile(true);
        }
        if(id!=0 && id != SecConst.EMPTY_ENDENTITYPROFILE) {
        	EndEntityProfileData eepd = EndEntityProfileData.findById(entityManager, Integer.valueOf(id));
        	if (eepd != null) {
        		returnval = eepd.getProfile();
        	} else {
        		// Ignore, but log, so we'll return null
        		log.debug("Did not find end entity profile with id: "+id);
        	}
        }
        if (log.isTraceEnabled()) {
            log.trace("<getEndEntityProfile(id): "+(returnval == null ? "null":"not null"));        	
        }
        return returnval;
    }

     /**
     * Finds a end entity profile by id.
     * 
     * @return EndEntityProfile or null if it does not exist
     * 
     * @ejb.transaction type="Supports"
     * @ejb.interface-method
     */
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public EndEntityProfile getEndEntityProfile(Admin admin, String profilename){
    	if (log.isTraceEnabled()) {
            log.trace(">getEndEntityProfile("+profilename+")");    		
    	}
        EndEntityProfile returnval=null;
        if(profilename.equals(EMPTY_ENDENTITYPROFILENAME)) {
        	returnval = new EndEntityProfile(true);
        } else {
        	EndEntityProfileData eepd = EndEntityProfileData.findByProfileName(entityManager, profilename);
        	if (eepd != null) {
        		returnval = eepd.getProfile();
        	} else {
        		// Ignore, but log, so we'll return null
        		log.debug("Did not find end entity profile with name: "+profilename);
        	}
        }
        if (log.isTraceEnabled()) {
        	log.trace("<getEndEntityProfile("+profilename+")");    		
        }
        return returnval;
    }

    /**
     * Returns a end entity profiles id, given it's profilename
     *
     * @return the id or 0 if profile cannot be found.
     * @ejb.transaction type="Supports"
     * @ejb.interface-method
     */
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public int getEndEntityProfileId(Admin admin, String profilename){
    	if (log.isTraceEnabled()) {
    		log.trace(">getEndEntityProfileId("+profilename+")");    		
    	}
    	int returnval = 0;
    	if(profilename.trim().equalsIgnoreCase(EMPTY_ENDENTITYPROFILENAME)) {
    		return SecConst.EMPTY_ENDENTITYPROFILE;
    	}
    	EndEntityProfileData eepd = EndEntityProfileData.findByProfileName(entityManager, profilename);
    	if (eepd != null) {
    		returnval = eepd.getId();
    	} else {
    		// Ignore so we'll return 0
    		if (log.isDebugEnabled()) {
    			log.debug("Did not find end entity profile with name: "+profilename);
    		}
    	}
    	if (log.isTraceEnabled()) {
    		log.trace("<getEndEntityProfileId("+profilename+")");    		
    	}
    	return returnval;
    }

    /**
     * Returns a end entity profiles name given it's id.
     *
     * @return profilename or null if profile id doesn't exists.
     * @ejb.transaction type="Supports"
     * @ejb.interface-method
     */
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public String getEndEntityProfileName(Admin admin, int id){
    	String returnval = null;
    	if(id == SecConst.EMPTY_ENDENTITYPROFILE) {
    		return EMPTY_ENDENTITYPROFILENAME;
    	}
    	EndEntityProfileData eepd = EndEntityProfileData.findById(entityManager, Integer.valueOf(id));
    	if (eepd != null) {
    		returnval = eepd.getProfileName();
    	} else {
    		if (log.isDebugEnabled()) {
    			log.debug("Did not find end entity profile with id: "+id);
    		}
    	}
    	return returnval;
    }

    /**
     * Method to check if a certificateprofile exists in any of the end entity profiles. Used to avoid desyncronization of certificate profile data.
     *
     * @param certificateprofileid the certificatetype id to search for.
     * @return true if certificateprofile exists in any of the end entity profiles.
     * @ejb.transaction type="Supports"
     * @ejb.interface-method
     */
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public boolean existsCertificateProfileInEndEntityProfiles(Admin admin, int certificateprofileid){
    	String[] availablecertprofiles=null;
    	boolean exists = false;
    	Collection<EndEntityProfileData> result = EndEntityProfileData.findAll(entityManager);
    	Iterator<EndEntityProfileData> i = result.iterator();
    	while(i.hasNext() && !exists){
    		availablecertprofiles = i.next().getProfile().getValue(EndEntityProfile.AVAILCERTPROFILES, 0).split(EndEntityProfile.SPLITCHAR);
    		for(int j=0; j < availablecertprofiles.length; j++){
    			if(Integer.parseInt(availablecertprofiles[j]) == certificateprofileid){
    				exists=true;
    				break;
    			}
    		}
    	}
    	return exists;
    }

    /**
     * Method to check if a CA exists in any of the end entity profiles. Used to avoid desyncronization of CA data.
     *
     * @param caid the caid to search for.
     * @return true if ca exists in any of the end entity profiles.
     * @ejb.transaction type="Supports"
     * @ejb.interface-method
     */
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public boolean existsCAInEndEntityProfiles(Admin admin, int caid){
    	String[] availablecas=null;
    	boolean exists = false;
    	Collection<EndEntityProfileData> result = EndEntityProfileData.findAll(entityManager); 
    	Iterator<EndEntityProfileData> i = result.iterator();
    	while(i.hasNext() && !exists){
    		EndEntityProfileData ep = i.next();
    		availablecas = ep.getProfile().getValue(EndEntityProfile.AVAILCAS, 0).split(EndEntityProfile.SPLITCHAR);
    		for(int j=0; j < availablecas.length; j++){
    			if(Integer.parseInt(availablecas[j]) == caid){
    				exists=true;
    				if (log.isDebugEnabled()) {
    					log.debug("CA exists in entity profile "+ep.getProfileName());
    				}
    				break;
    			}
    		}
    	}
    	return exists;
    }

    /**
     * Loads the global configuration from the database.
     *
     * @throws EJBException if a communication or other error occurs.
     * @ejb.transaction type="Supports"
     * @ejb.interface-method
     */
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public GlobalConfiguration loadGlobalConfiguration(Admin admin)  {
        try {
        	if (log.isTraceEnabled()) {
        		log.trace(">loadGlobalConfiguration()");
        	}
            // Only do the actual SQL query if we might update the configuration due to cache time anyhow
            if ( globalconfiguration!=null && lastupdatetime+MIN_TIME_BETWEEN_GLOBCONF_UPDATES > new Date().getTime() ){
                return globalconfiguration;
            }
            try{
                log.debug("Reading GlobalConfiguration");
                GlobalConfigurationData gcdata = GlobalConfigurationData.findByConfigurationId(entityManager, "0");
                if (gcdata != null) {
                    globalconfiguration = gcdata.getGlobalConfiguration();
                    lastupdatetime = new Date().getTime();
                } else {
                    log.debug("No default GlobalConfiguration exists. Trying to create a new one.");
                    saveGlobalConfiguration(admin, new GlobalConfiguration());
                    lastupdatetime = new Date().getTime();
                }
            }catch (Throwable t) {
                log.error("Failed to load global configuration", t);
			}
            if ( globalconfiguration!=null ) {
                return globalconfiguration;
            }
            return new GlobalConfiguration();	// Fallback to create a new unsaved config
        } finally {
        	if (log.isTraceEnabled()) {
        		log.trace("<loadGlobalConfiguration()");
        	}
        }
    }

    /**
     * Saves the globalconfiguration
     *
     * @throws EJBException if a communication or other error occurs.
     * @ejb.interface-method
     */
    public void saveGlobalConfiguration(Admin admin, GlobalConfiguration globalconfiguration)  {
    	if (log.isTraceEnabled()) {
    		log.trace(">saveGlobalConfiguration()");
    	}
    	String pk = "0";
    	GlobalConfigurationData gcdata = GlobalConfigurationData.findByConfigurationId(entityManager, pk);
    	if (gcdata != null) {
    		gcdata.setGlobalConfiguration(globalconfiguration);
			String msg = intres.getLocalizedMessage("ra.savedconf", gcdata.getConfigurationId());            	
    		logSession.log(admin, admin.getCaId(), LogConstants.MODULE_RA, new java.util.Date(),null, null, LogConstants.EVENT_INFO_EDITSYSTEMCONFIGURATION,msg);
    	} else {
    		// Global configuration doesn't yet exists.
    		try {
    			entityManager.persist(new GlobalConfigurationData(pk,globalconfiguration));
    			String msg = intres.getLocalizedMessage("ra.createdconf", pk);            	
    			logSession.log(admin, admin.getCaId(), LogConstants.MODULE_RA, new java.util.Date(),null, null, LogConstants.EVENT_INFO_EDITSYSTEMCONFIGURATION, msg);
    		} catch(Exception e) {
    			String msg = intres.getLocalizedMessage("ra.errorcreateconf");            	
    			logSession.log(admin, admin.getCaId(), LogConstants.MODULE_RA, new java.util.Date(),null, null, LogConstants.EVENT_ERROR_EDITSYSTEMCONFIGURATION,msg);
    		}
    	}
    	this.globalconfiguration = globalconfiguration;
    	if (log.isTraceEnabled()) {
    		log.trace("<saveGlobalConfiguration()");
    	}
    }

    /**
     * @ejb.interface-method
     */
    public int findFreeEndEntityProfileId(){
    	int id = getRandomInt();
    	boolean foundfree = false;
    	while(!foundfree){
    		if(id > 1) {
    			if (EndEntityProfileData.findById(entityManager, Integer.valueOf(id)) == null) {
    				foundfree = true;
    			}
    		}
    		id++;
    	}
    	return id;
    }


    // Private methods

    private static Random random = null;
    /** Helper to re-use a Random object */
    private int getRandomInt() {
    	if (random == null) {
    		random = new Random(new Date().getTime());
    	}
    	return random.nextInt();
    }
    
    private boolean isFreeEndEntityProfileId(int id) {
    	boolean foundfree = false;
    	if (id > 1) {
    		if (EndEntityProfileData.findById(entityManager, Integer.valueOf(id)) == null) {
    			foundfree = true;
    		}
    	}
    	return foundfree;
    }

    /**
     * Changes the admin preference in the database. Returns false if admin preference doesn't exist.
     */
    private boolean updateAdminPreference(Admin admin, String certificatefingerprint, AdminPreference adminpreference, boolean dolog){
    	if (log.isTraceEnabled()) {
    		log.trace(">updateAdminPreference(fingerprint : " + certificatefingerprint + ")");
    	}
    	boolean ret = false;
    	AdminPreferencesData apdata1 = AdminPreferencesData.findById(entityManager, certificatefingerprint);
    	if (apdata1 != null) {
        	apdata1.setAdminPreference(adminpreference);
        	// Earlier we used to remove and re-add the adminpreferences data
        	// I don't know why, but that did not work on Oracle AS, so lets just do what create does, and setAdminPreference.
        	/*
            adminpreferenceshome.remove(certificatefingerprint);
            try{
                AdminPreferencesDataLocal apdata2 = adminpreferenceshome.findByPrimaryKey(certificatefingerprint);
                debug("Found admin preferences with id: "+apdata2.getId());
            }  catch (javax.ejb.FinderException fe) {
            	debug("Admin preferences has been removed: "+certificatefingerprint);
            }
            adminpreferenceshome.create(certificatefingerprint,adminpreference);
            try{
                AdminPreferencesDataLocal apdata3 = adminpreferenceshome.findByPrimaryKey(certificatefingerprint);
                debug("Found admin preferences with id: "+apdata3.getId());
            }  catch (javax.ejb.FinderException fe) {
            	error("Admin preferences was not created: "+certificatefingerprint);
            }
            */
            if (dolog) {                
    			String msg = intres.getLocalizedMessage("ra.changedadminpref", certificatefingerprint);            	
                logSession.log(admin, admin.getCaId(), LogConstants.MODULE_RA, new java.util.Date(),null, null, LogConstants.EVENT_INFO_ADMINISTRATORPREFERENCECHANGED,msg);
            }
            ret = true;
    	} else {
             ret=false;
             if (dolog) {
            	 String msg = intres.getLocalizedMessage("ra.adminprefnotfound", certificatefingerprint);            	
                 logSession.log(admin,admin.getCaId(), LogConstants.MODULE_RA, new java.util.Date(),null, null, LogConstants.EVENT_ERROR_ADMINISTRATORPREFERENCECHANGED,msg);
             }
        }
    	if (log.isTraceEnabled()) {
    		log.trace("<updateAdminPreference()");
    	}
        return ret;
    }
}
