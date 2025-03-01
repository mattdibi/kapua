/*******************************************************************************
 * Copyright (c) 2020, 2022 Eurotech and/or its affiliates and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Eurotech - initial API and implementation
 *******************************************************************************/
package org.eclipse.kapua.service.authentication.credential.mfa.shiro;

import java.util.Date;
import java.util.List;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.PostLoad;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import com.google.common.base.Strings;
import org.eclipse.kapua.commons.jpa.SecretAttributeConverter;
import org.eclipse.kapua.commons.model.AbstractKapuaUpdatableEntity;
import org.eclipse.kapua.commons.model.id.KapuaEid;
import org.eclipse.kapua.model.id.KapuaId;
import org.eclipse.kapua.service.authentication.credential.mfa.MfaOption;
import org.eclipse.kapua.service.authentication.credential.mfa.ScratchCode;

/**
 * {@link MfaOption} implementation.
 *
 * @since 1.3.0
 */
@Entity(name = "MfaOption")
@Table(name = "atht_mfa_option")
public class MfaOptionImpl extends AbstractKapuaUpdatableEntity implements MfaOption {

    private static final long serialVersionUID = -1872939877726584407L;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "eid", column = @Column(name = "user_id", updatable = false, nullable = false))
    })
    private KapuaEid userId;

    @Basic
    @Column(name = "mfa_secret_key", nullable = false)
    @Convert(converter = SecretAttributeConverter.class)
    private String mfaSecretKey;

    @Basic
    @Column(name = "trust_key")
    private String trustKey;

    @Basic
    @Column(name = "has_trust_me")
    private Boolean hasTrustMe;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "trust_expiration_date")
    protected Date trustExpirationDate;

    @Transient
    private String qrCodeImage;

    @Transient
    private List<ScratchCode> scratchCodes;

    /**
     * Constructor.
     *
     * @since 1.3.0
     */
    public MfaOptionImpl() {
        super();
    }

    /**
     * Constructor.
     *
     * @param scopeId
     *         The {@link MfaOption#getScopeId()}
     * @since 1.3.0
     */
    public MfaOptionImpl(KapuaId scopeId) {
        super(scopeId);
    }

    /**
     * Constructor.
     *
     * @param scopeId
     *         The scope {@link KapuaId} to set into the {@link MfaOption}.
     * @param userId
     *         user identifier
     * @param mfaSecretKey
     *         The secret key to set into the {@link MfaOption}.
     * @since 1.3.0
     * @deprecated Since 2.0.0. Please make use of  {@link #MfaOptionImpl()} and its setters.
     */
    @Deprecated
    public MfaOptionImpl(KapuaId scopeId, KapuaId userId, String mfaSecretKey) {
        super(scopeId);

        this.userId = KapuaEid.parseKapuaId(userId);
        this.mfaSecretKey = mfaSecretKey;
    }

    /**
     * Clone constructor.
     *
     * @param mfaOption
     *         The {@link MfaOption} to clone.
     * @since 1.3.0
     */
    public MfaOptionImpl(MfaOption mfaOption) {
        super(mfaOption);
        setUserId(mfaOption.getUserId());
        setMfaSecretKey(mfaOption.getMfaSecretKey());
        setTrustKey(mfaOption.getTrustKey());
        setHasTrustMe(mfaOption.getHasTrustMe());
        setTrustExpirationDate(mfaOption.getTrustExpirationDate());
    }

    @Override
    public KapuaId getUserId() {
        return userId;
    }

    @Override
    public void setUserId(KapuaId userId) {
        this.userId = KapuaEid.parseKapuaId(userId);
    }

    @Override
    public String getMfaSecretKey() {
        return mfaSecretKey;
    }

    @Override
    public void setMfaSecretKey(String mfaSecretKey) {
        this.mfaSecretKey = mfaSecretKey;
    }

    @Override
    public String getTrustKey() {
        return this.trustKey;
    }

    @Override
    public void setTrustKey(String trustKey) {
        this.trustKey = trustKey;
    }

    @Override
    public boolean getHasTrustMe() {
        return hasTrustMe;
    }

    public void setHasTrustMe(boolean hasTrustMe) {
        this.hasTrustMe = hasTrustMe;
    }

    @Override
    public Date getTrustExpirationDate() {
        return trustExpirationDate;
    }

    @Override
    public void setTrustExpirationDate(Date trustExpirationDate) {
        this.trustExpirationDate = trustExpirationDate;
    }

    @Override
    public String getQRCodeImage() {
        return qrCodeImage;
    }

    @Override
    public void setQRCodeImage(String qrCodeImage) {
        this.qrCodeImage = qrCodeImage;
    }

    @Override
    public List<ScratchCode> getScratchCodes() {
        return scratchCodes;
    }

    @Override
    public void setScratchCodes(List<ScratchCode> scratchCodes) {
        this.scratchCodes = scratchCodes;
    }

    /**
     * Set {@link #getHasTrustMe()} according to the value of {@link #getTrustKey()} before create
     *
     * @since 2.1.0
     */
    @PrePersist
    protected void prePersistsAction() {
        super.prePersistsAction();

        setHasTrustMe(!Strings.isNullOrEmpty(getTrustKey()));
    }

    /**
     * Set {@link #getHasTrustMe()} according to the value of {@link #getTrustKey()} before update
     *
     * @since 2.1.0
     */
    @PreUpdate
    protected void preUpdateAction() {
        super.preUpdateAction();

        setHasTrustMe(!Strings.isNullOrEmpty(getTrustKey()));
    }

    /**
     * Sets {@link #getHasTrustMe()} for all {@link MfaOption}s existing before the adding of it.
     *
     * @since 2.1.0
     */
    @PostLoad
    protected void postLoadAction() {
        if (hasTrustMe == null) {
            setHasTrustMe(!Strings.isNullOrEmpty(getTrustKey()));
        }
    }
}
