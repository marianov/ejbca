/*************************************************************************
 *                                                                       *
 *  CESeCore: CE Security Core                                           *
 *                                                                       *
 *  This software is free software; you can redistribute it and/or       *
 *  modify it under the terms of the GNU Lesser General Public           *
 *  License as published by the Free Software Foundation; either         *
 *  version 2.1 of the License, or any later version.                    *
 *                                                                       *
 *  See terms of license at gnu.org.                                     *
 *                                                                       *
 *************************************************************************/
package org.cesecore.certificates.certificate;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.persistence.ColumnResult;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.PostLoad;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.SqlResultSetMapping;
import javax.persistence.SqlResultSetMappings;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.TypedQuery;

import org.apache.log4j.Logger;
import org.cesecore.dbprotection.ProtectionStringBuilder;
import org.cesecore.util.CertTools;
import org.cesecore.util.StringTools;

/**
 * Representation of a revoked throw-away certificate and related information.
 *
 * @version $Id: NoConflictCertificateData.java 28264 2018-04-09 15:56:54Z tarmo $
 */
@Entity
@Table(name = "NoConflictCertificateData")
@SqlResultSetMappings(value = {
        @SqlResultSetMapping(name = "RevokedNoConflictCertInfoSubset", columns = { @ColumnResult(name = "fingerprint"), @ColumnResult(name = "serialNumber"),
                @ColumnResult(name = "expireDate"), @ColumnResult(name = "revocationDate"), @ColumnResult(name = "revocationReason") }),
        @SqlResultSetMapping(name = "NoConflictCertificateInfoSubset", columns = { @ColumnResult(name = "issuerDN"), @ColumnResult(name = "subjectDN"),
                @ColumnResult(name = "cAFingerprint"), @ColumnResult(name = "status"), @ColumnResult(name = "type"),
                @ColumnResult(name = "serialNumber"),
                @ColumnResult(name = "notBefore"), @ColumnResult(name = "expireDate"), @ColumnResult(name = "revocationDate"),
                @ColumnResult(name = "revocationReason"), @ColumnResult(name = "username"), @ColumnResult(name = "tag"),
                @ColumnResult(name = "certificateProfileId"), @ColumnResult(name = "endEntityProfileId"), @ColumnResult(name = "updateTime"),
                @ColumnResult(name = "subjectKeyId"), @ColumnResult(name = "subjectAltName") }),
        @SqlResultSetMapping(name = "NoConflictCertificateInfoSubset2", columns = { @ColumnResult(name = "fingerprint"), @ColumnResult(name = "subjectDN"),
                @ColumnResult(name = "cAFingerprint"), @ColumnResult(name = "status"), @ColumnResult(name = "type"),
                @ColumnResult(name = "notBefore"), @ColumnResult(name = "expireDate"), @ColumnResult(name = "revocationDate"),
                @ColumnResult(name = "revocationReason"), @ColumnResult(name = "username"), @ColumnResult(name = "tag"),
                @ColumnResult(name = "certificateProfileId"), @ColumnResult(name = "endEntityProfileId"), @ColumnResult(name = "updateTime"),
                @ColumnResult(name = "subjectKeyId"), @ColumnResult(name = "subjectAltName") }),
        @SqlResultSetMapping(name = "NoConflictCertificateFingerprintUsernameSubset", columns = { @ColumnResult(name = "fingerprint"), @ColumnResult(name = "username") }) })
public class NoConflictCertificateData extends BaseCertificateData implements Serializable {
    
    private static final long serialVersionUID = 1L;

    private static final Logger log = Logger.getLogger(NoConflictCertificateData.class);

    private String id;
    private String issuerDN;
    private String subjectDN;
    private String subjectAltName = null;  // @since EJBCA 6.6.0
    private String fingerprint = "";
    private String cAFingerprint;
    private int status = 0;
    private int type = 0;
    private String serialNumber;
    private Long notBefore = null;  // @since EJBCA 6.6.0
    private long expireDate = 0;
    private long revocationDate = 0;
    private int revocationReason = 0;
    private String base64Cert;
    private String username;
    private String tag;
    private Integer certificateProfileId;
    private Integer endEntityProfileId = null;  // @since EJBCA 6.6.0
    private long updateTime = 0;
    private String subjectKeyId;
    private int rowVersion = 0;
    private String rowProtection;
    
    
    /**
     * Copy Constructor
     */
    public NoConflictCertificateData(final NoConflictCertificateData copy) {
        setId(copy.getId());
        setBase64Cert(copy.getBase64Cert());
        setFingerprint(copy.getFingerprint());
        setSubjectDN(copy.getSubjectDN());
        setIssuerDN(copy.getIssuerDN());
        setSubjectAltName(copy.getSubjectAltName());
        setSerialNumber(copy.getSerialNumber());
        setUsername(copy.getUsername());
        setStatus(copy.getStatus());
        setType(copy.getType());
        setCaFingerprint(copy.getCaFingerprint());
        setNotBefore(copy.getNotBefore());
        setExpireDate(copy.getExpireDate());
        setRevocationDate(copy.getRevocationDate());
        setRevocationReason(copy.getRevocationReason());
        setUpdateTime(copy.getUpdateTime());
        setCertificateProfileId(copy.getCertificateProfileId());
        setEndEntityProfileId(copy.getEndEntityProfileId());
        setSubjectKeyId(copy.getSubjectKeyId());
        setTag(copy.getTag());
        setRowVersion(copy.getRowVersion());
        setRowProtection(copy.getRowProtection());
    }

    public NoConflictCertificateData() {
        
    }
    
    /**
     * Generated GUID for the table entry
     * 
     * @return id
     */
    public String getId() {
        return id;
    }

    /** Generated GUID for the table entry
     * 
     * @param id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Fingerprint of certificate
     *
     * @return fingerprint
     */
    @Override
    public String getFingerprint() {
        return fingerprint;
    }

    /**
     * Fingerprint of certificate
     *
     * @param fingerprint fingerprint
     */
    public void setFingerprint(String fingerprint) {
        this.fingerprint = fingerprint;
    }

    /**
     * DN of issuer of certificate
     *
     * @return issuer dn
     */
    @Override
    public String getIssuerDN() {
        return issuerDN;
    }

    /**
     * Use setIssuer instead
     *
     * @param issuerDN issuer dn
     * @see #setIssuer(String)
     */
    public void setIssuerDN(String issuerDN) {
        this.issuerDN = issuerDN;
    }

    /**
     * Use getSubjectDnNeverNull() for consistent access, since Oracle will treat empty Strings as NULL.
     *
     * @return value as it is stored in the database
     */
    @Override
    public String getSubjectDN() {
        return subjectDN;
    }

    /**
     * Use setSubject instead
     *
     * @param subjectDN subject dn
     * @see #setSubject(String)
     */
    public void setSubjectDN(String subjectDN) {
        this.subjectDN = subjectDN;
    }

    /** @return Subject Alternative Name from the certificate if it was saved at the time of issuance. */
    @Transient
    public String getSubjectAltNameNeverNull() {
        final String subjectAltName = getSubjectAltName();
        return subjectAltName == null ? "" : subjectAltName;
    }

    /**
     * Use getSubjectAltNameNeverNull() for consistent access, since Oracle will treat empty Strings as null.
     *
     * @return value as it is stored in the database
     */
    @Override
    public String getSubjectAltName() {
        return subjectAltName;
    }
    public void setSubjectAltName(final String subjectAltName) {
        this.subjectAltName = subjectAltName;
    }

    /**
     * Fingerprint of CA certificate
     *
     * @return fingerprint
     */
    @Override
    public String getCaFingerprint() {
        return cAFingerprint;
    }

    /**
     * Fingerprint of CA certificate
     *
     * @param cAFingerprint fingerprint
     */
    public void setCaFingerprint(String cAFingerprint) {
        this.cAFingerprint = cAFingerprint;
    }

    /**
     * status of certificate, ex CertificateConstants.CERT_ACTIVE
     *
     * @see CertificateConstants#CERT_ACTIVE etc
     *
     * @return status
     */
    @Override
    public int getStatus() {
        return status;
    }

    /**
     * status of certificate, ex CertificateConstants.CERT_ACTIVE
     *
     * @param status status
     */
    @Override
    public void setStatus(int status) {
        this.status = status;
    }

    /**
     * What type of user the certificate belongs to, ex CertificateConstants.CERTTYPE_ENDENTITY
     *
     * @return user type
     */
    @Override
    public int getType() {
        return type;
    }

    /**
     * What type of user the certificate belongs to, ex CertificateConstants.CERTTYPE_ENDENTITY
     *
     * @param type type
     */
    public void setType(int type) {
        this.type = type;
    }

    /**
     * Serialnumber formated as BigInteger.toString()
     *
     * @return serial number
     */
    @Override
    public String getSerialNumber() {
        return serialNumber;
    }

    /**
     * Sets serial number (formated as BigInteger.toString())
     *
     * @param serialNumber serial number formated as BigInteger.toString()
     */
    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    /** @returns the number of milliseconds since 1970-01-01 00:00:00 GMT until the certificate was issued or null if the information is not known. */
    @Override
    public Long getNotBefore() {
        return notBefore;
    }
    public void setNotBefore(final Long notBefore) {
        this.notBefore = notBefore;
    }

    /** @returns the number of milliseconds since 1970-01-01 00:00:00 GMT until the certificate expires. */
    @Override
    public long getExpireDate() {
        return expireDate;
    }

    /**
     * Date formated as milliseconds since 1970 (== Date.getTime())
     *
     * @param expireDate expire date
     */
    public void setExpireDate(long expireDate) {
        this.expireDate = expireDate;
    }

    /**
     * Set to date when revocation occured if status == CERT_REVOKED. Format == Date.getTime()
     *
     * @return revocation date
     */
    @Override
    public long getRevocationDate() {
        return revocationDate;
    }

    /**
     * Set to date when revocation occurred if status == CERT_REVOKED. Format == Date.getTime()
     *
     * @param revocationDate revocation date
     */
    public void setRevocationDate(long revocationDate) {
        this.revocationDate = revocationDate;
    }

    /**
     * Set to revocation reason if status == CERT_REVOKED
     *
     * @return revocation reason
     */
    @Override
    public int getRevocationReason() {
        return revocationReason;
    }

    /**
     * Set to revocation reason if status == CERT_REVOKED
     *
     * @param revocationReason revocation reason
     */
    public void setRevocationReason(int revocationReason) {
        this.revocationReason = revocationReason;
    }

    /**
     * The certificate itself
     *
     * @return base64 encoded certificate
     */
    public String getBase64Cert() {
        return this.getZzzBase64Cert();
    }

    /**
     * The certificate itself
     *
     * @param base64Cert base64 encoded certificate
     */
    public void setBase64Cert(String base64Cert) {
        this.setZzzBase64Cert(base64Cert);
    }

    /**
     * Horrible work-around due to the fact that Oracle needs to have (LONG and) CLOB values last in order to avoid ORA-24816.
     *
     * Since Hibernate sorts columns by the property names, naming this Z-something will apparently ensure that this column is used last.
     * @deprecated Use {@link #getBase64Cert()} instead
     */
    @Deprecated
    public String getZzzBase64Cert() {
        return base64Cert;
    }
    
    /** @deprecated Use {@link #setBase64Cert(String)} instead */
    @Deprecated
    public void setZzzBase64Cert(final String zzzBase64Cert) {
        this.base64Cert = zzzBase64Cert;
    }

    /**
     * username in database
     *
     * @return username
     */
    @Override
    public String getUsername() {
        return username;
    }

    /**
     * username in database
     *
     * @param username username
     */
    public void setUsername(String username) {
        this.username = StringTools.stripUsername(username);
    }

    /**
     * tag in database. This field was added for the 3.9.0 release, but is not used yet.
     *
     * @return tag
     */
    @Override
    public String getTag() {
        return tag;
    }

    /**
     * tag in database. This field was added for the 3.9.0 release, but is not used yet.
     *
     * @param username tag
     */
    public void setTag(String tag) {
        this.tag = tag;
    }

    /**
     * Certificate Profile Id that was used to issue this certificate.
     *
     * @return certificateProfileId
     */
    @Override
    public Integer getCertificateProfileId() {
        return certificateProfileId;
    }

    /**
     * Certificate Profile Id that was used to issue this certificate.
     *
     * @param certificateProfileId certificateProfileId
     */
    public void setCertificateProfileId(Integer certificateProfileId) {
        this.certificateProfileId = certificateProfileId;
    }

    /**
     * The time this row was last updated.
     *
     * @return updateTime
     */
    @Override
    public Long getUpdateTime() {
        return updateTime;
    }

    /**
     * The time this row was last updated.
     */
    // Hibernate + Oracle ignores nullable=false so we can expect null-objects as input after upgrade. TODO: Verify if still true!
    public void setUpdateTime(Long updateTime) {
        this.updateTime = (updateTime == null ? this.updateTime : updateTime);
    }

    /**
     * The ID of the public key of the certificate
     */
    @Override
    public String getSubjectKeyId() {
        return subjectKeyId;
    }

    /**
     * The ID of the public key of the certificate
     */
    public void setSubjectKeyId(String subjectKeyId) {
        this.subjectKeyId = subjectKeyId;
    }
    
    public int getRowVersion() {
        return rowVersion;
    }

    public void setRowVersion(int rowVersion) {
        this.rowVersion = rowVersion;
    }
    
    @Override
    public String getRowProtection() {
        return this.getZzzRowProtection();
    }

    @Override
    public void setRowProtection(String rowProtection) {
        this.setZzzRowProtection(rowProtection);
    }

    /**
     * Horrible work-around due to the fact that Oracle needs to have (LONG and) CLOB values last in order to avoid ORA-24816.
     *
     * Since Hibernate sorts columns by the property names, naming this Z-something will apparently ensure that this column is used last.
     * @deprecated Use {@link #getRowProtection()} instead
     */
    @Deprecated
    public String getZzzRowProtection() {
        return rowProtection;
    }
    /** @deprecated Use {@link #setRowProtection(String)} instead */
    @Deprecated
    public void setZzzRowProtection(final String zzzRowProtection) {
        this.rowProtection = zzzRowProtection;
    }
    

    /**
     * DN of issuer of certificate
     *
     * @param dn issuer dn
     */
    public void setIssuer(String dn) {
        setIssuerDN(CertTools.stringToBCDNString(dn));
    }

    /**
     * DN of subject in certificate
     *
     * @param dn subject dn
     */
    public void setSubject(String dn) {
        setSubjectDN(CertTools.stringToBCDNString(dn));
    }

    /**
     * expire date of certificate
     *
     * @param expireDate expire date
     */
    public void setExpireDate(Date expireDate) {
        if (expireDate == null) {
            setExpireDate(-1L);
        } else {
            setExpireDate(expireDate.getTime());
        }
    }

    /**
     * date the certificate was revoked
     *
     * @param revocationDate revocation date
     */
    public void setRevocationDate(Date revocationDate) {
        if (revocationDate == null) {
            setRevocationDate(-1L);
        } else {
            setRevocationDate(revocationDate.getTime());
        }
    }

    public void setEndEntityProfileId(final Integer endEntityProfileId) {
        this.endEntityProfileId = endEntityProfileId;
    }
    
    /** @return the end entity profile this certificate was issued under or null if the information is not available. */
    @Override
    public Integer getEndEntityProfileId() {
        return endEntityProfileId;
    }
    
    
    
    // Comparators

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof NoConflictCertificateData)) {
            return false;
        }
        return equals((NoConflictCertificateData) obj, true);
    }

    public boolean equals(NoConflictCertificateData certificateData, boolean mode, boolean strictStatus) {
        if (mode) {
            return equalsNonSensitive(certificateData, strictStatus);
        }
        return equals(certificateData, strictStatus);
    }

    private boolean equals(NoConflictCertificateData certificateData, boolean strictStatus) {
        if (!equalsNonSensitive(certificateData, strictStatus)) {
            return false;
        }
        if ( this.base64Cert==null && certificateData.base64Cert==null ) {
            return true; // test before shows that fingerprint is equal and then both objects must refer to same row in Base64CertData
        }
        if ( this.base64Cert==null || certificateData.base64Cert==null ) {
            return false; // one is null and the other not null
        }
        if (!this.base64Cert.equals(certificateData.base64Cert)) {
            return false;
        }
        return true;
    }

    private boolean equalsNonSensitive(NoConflictCertificateData certificateData, boolean strictStatus) {
        
        if (!id.equals(certificateData.id)) {
            return false;
        }
        if (!issuerDN.equals(certificateData.issuerDN)) {
            return false;
        }
        if (!subjectDN.equals(certificateData.subjectDN)) {
            return false;
        }
        if (!fingerprint.equals(certificateData.fingerprint)) {
            return false;
        }
        if (!cAFingerprint.equals(certificateData.cAFingerprint)) {
            return false;
        }
        if (!equalsStatus(certificateData, strictStatus)) {
            return false;
        }
        if (type != certificateData.type) {
            return false;
        }
        if (!serialNumber.equals(certificateData.serialNumber)) {
            return false;
        }
        if (notBefore==null) {
            if (certificateData.notBefore!=null) {
                return false;
            }
        } else {
            if (!notBefore.equals(certificateData.notBefore)) {
                return false;
            }
        }
        if (expireDate != certificateData.expireDate) {
            return false;
        }
        if (revocationDate != certificateData.revocationDate) {
            return false;
        }
        if (revocationReason != certificateData.revocationReason) {
            return false;
        }
        if (!username.equals(certificateData.username)) {
            return false;
        }
        if (tag == null && certificateData.tag != null) {
            return false;
        }
        if (tag != null && !tag.equals(certificateData.tag)) {
            return false;
        }
        if (certificateProfileId == null && certificateData.certificateProfileId != null) {
            return false;
        }
        if (certificateProfileId != null && !certificateProfileId.equals(certificateData.certificateProfileId)) {
            return false;
        }
        if (endEntityProfileId==null) {
            if (certificateData.endEntityProfileId!=null) {
                return false;
            }
        } else {
            if (!endEntityProfileId.equals(certificateData.endEntityProfileId)) {
                return false;
            }
        }
        if (updateTime != certificateData.updateTime) {
            return false;
        }
        if (subjectAltName==null) {
            if (certificateData.subjectAltName!=null) {
                return false;
            }
        } else {
            if (!subjectAltName.equals(certificateData.subjectAltName)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Compare the status field of this and another NoConflictCertificateData object.
     *
     * @param strict will treat NOTIFIED as ACTIVE and ARCHIVED as REVOKED if set to false
     */
    public boolean equalsStatus(NoConflictCertificateData certificateData, boolean strict) {
        if (strict) {
            return status == certificateData.status;
        }
        if (status == certificateData.status) {
            return true;
        }
        if ((status == CertificateConstants.CERT_ACTIVE || status == CertificateConstants.CERT_NOTIFIEDABOUTEXPIRATION)
                && (certificateData.status == CertificateConstants.CERT_ACTIVE || certificateData.status == CertificateConstants.CERT_NOTIFIEDABOUTEXPIRATION)) {
            return true;
        }
        if ((status == CertificateConstants.CERT_REVOKED || status == CertificateConstants.CERT_ARCHIVED)
                && (certificateData.status == CertificateConstants.CERT_REVOKED || certificateData.status == CertificateConstants.CERT_ARCHIVED)) {
            return true;
        }
        return false;
    }

    public void updateWith(NoConflictCertificateData certificateData, boolean inclusionMode) {
        issuerDN = certificateData.issuerDN;
        subjectDN = certificateData.subjectDN;
        fingerprint = certificateData.fingerprint;
        cAFingerprint = certificateData.cAFingerprint;
        status = certificateData.status;
        type = certificateData.type;
        serialNumber = certificateData.serialNumber;
        expireDate = certificateData.expireDate;
        revocationDate = certificateData.revocationDate;
        revocationReason = certificateData.revocationReason;
        setUsername(certificateData.username);
        tag = certificateData.tag;
        certificateProfileId = certificateData.certificateProfileId;
        updateTime = certificateData.updateTime;
        base64Cert = inclusionMode ? null : certificateData.base64Cert;
        id = certificateData.id;
    }

    
    
    //
    // Search functions.
    //
    /** @deprecated Since 6.13.0. Use method in CertificateDataSession instead */
    @Deprecated
    public static List<NoConflictCertificateData> findByFingerprint(EntityManager entityManager, String fingerprint) {
        final TypedQuery<NoConflictCertificateData> query = entityManager.createQuery("SELECT a FROM NoConflictCertificateData a WHERE a.fingerprint=:fingerprint", NoConflictCertificateData.class);
        query.setParameter("fingerprint", fingerprint);
        return query.getResultList();
    }
    
    /** @deprecated Since 6.13.0. Use method in CertificateDataSession instead */
    @Deprecated
    public static List<NoConflictCertificateData> findBySerialNumber(EntityManager entityManager, String serialNumber) {
        final TypedQuery<NoConflictCertificateData> query = entityManager.createQuery("SELECT a FROM NoConflictCertificateData a WHERE a.serialNumber=:serialNumber", NoConflictCertificateData.class);
        query.setParameter("serialNumber", serialNumber);
        return query.getResultList();
    }
    
    /** @deprecated Since 6.13.0. Use method in CertificateDataSession instead */
    @Deprecated
    public static List<NoConflictCertificateData> findByIssuerDNSerialNumber(EntityManager entityManager, String issuerDN, String serialNumber) {
        String sql = "SELECT a FROM NoConflictCertificateData a WHERE a.issuerDN=:issuerDN AND a.serialNumber=:serialNumber";
        final TypedQuery<NoConflictCertificateData> query = entityManager.createQuery(sql, NoConflictCertificateData.class);
        query.setParameter("issuerDN", issuerDN);
        query.setParameter("serialNumber", serialNumber);
        return query.getResultList();
    }
    
    

    //
    // Start Database integrity protection methods
    //

    @Transient
    @Override
    protected String getProtectString(final int version) {
    	final ProtectionStringBuilder build = new ProtectionStringBuilder(3000);
        // What is important to protect here is the data that we define, id, name and certificate profile data
        // rowVersion is automatically updated by JPA, so it's not important, it is only used for optimistic locking
        build.append(getFingerprint()).append(getIssuerDN());
        if (version>=3) {
            // From version 3 for EJBCA 6.7 we always use empty String here to allow future migration between databases when this value is unset
            build.append(getSubjectDnNeverNull());
        } else {
            build.append(getSubjectDN());
        }
        build.append(getCaFingerprint()).append(getStatus()).append(getType())
                .append(getSerialNumber()).append(getExpireDate()).append(getRevocationDate()).append(getRevocationReason()).append(getBase64Cert())
                .append(getUsername()).append(getTag()).append(getCertificateProfileId()).append(getUpdateTime()).append(getSubjectKeyId());
        if (version>=2) {
            // In version 2 for EJBCA 6.6 the following columns where added
            build.append(String.valueOf(getNotBefore()));
            build.append(String.valueOf(getEndEntityProfileId()));
            if (version>=3) {
                // From version 3 for EJBCA 6.7 we always use empty String here to allow future migration between databases when this value is unset
                build.append(getSubjectAltNameNeverNull());
            } else {
                build.append(String.valueOf(getSubjectAltName()));
            }
        }
        if (log.isDebugEnabled()) {
            // Some profiling
            if (build.length() > 3000) {
                log.debug("CertificateData.getProtectString gives size: " + build.length());
            }
        }
        return build.toString();
    }

    @Transient
    @Override
    protected int getProtectVersion() {
        return 3;
    }

    @PrePersist
    @PreUpdate
    @Override
    protected void protectData() {
        super.protectData();
    }

    @PostLoad
    @Override
    protected void verifyData() {
        super.verifyData();
    }

    @Override
    @Transient
    protected String getRowId() {
        return getFingerprint();
    }
    
    //
    // End Database integrity protection methods
    //
}