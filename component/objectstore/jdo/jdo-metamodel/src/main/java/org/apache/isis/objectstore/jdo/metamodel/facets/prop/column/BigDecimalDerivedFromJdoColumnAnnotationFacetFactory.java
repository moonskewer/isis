/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.isis.objectstore.jdo.metamodel.facets.prop.column;

import java.math.BigDecimal;
import java.util.List;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdentityType;

import com.google.common.base.Objects;

import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facetapi.MetaModelValidatorRefiner;
import org.apache.isis.core.metamodel.facets.Annotations;
import org.apache.isis.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.core.metamodel.facets.FacetedMethod;
import org.apache.isis.core.metamodel.facets.mandatory.MandatoryFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.Contributed;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelValidatorComposite;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelValidatorVisiting;
import org.apache.isis.core.metamodel.specloader.validator.ValidationFailures;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelValidatorVisiting.Visitor;
import org.apache.isis.core.progmodel.facets.properties.javaxvaldigits.BigDecimalFacetForPropertyFromJavaxValidationDigitsAnnotation;
import org.apache.isis.core.progmodel.facets.value.bigdecimal.BigDecimalValueFacet;
import org.apache.isis.core.progmodel.facets.value.bigdecimal.BigDecimalValueSemanticsProvider;
import org.apache.isis.objectstore.jdo.metamodel.facets.object.persistencecapable.JdoPersistenceCapableFacet;
import org.apache.isis.objectstore.jdo.metamodel.facets.prop.notpersistent.JdoNotPersistentFacet;


public class BigDecimalDerivedFromJdoColumnAnnotationFacetFactory extends FacetFactoryAbstract implements MetaModelValidatorRefiner {

    private static final int DEFAULT_LENGTH = BigDecimalValueSemanticsProvider.DEFAULT_LENGTH;
    private static final int DEFAULT_SCALE = BigDecimalValueSemanticsProvider.DEFAULT_SCALE;

    public BigDecimalDerivedFromJdoColumnAnnotationFacetFactory() {
        super(FeatureType.PROPERTIES_ONLY);
    }

    @Override
    public void process(final ProcessMethodContext processMethodContext) {

        if(BigDecimal.class != processMethodContext.getMethod().getReturnType()) {
            return;
        }
        
        final FacetedMethod holder = processMethodContext.getFacetHolder();

        // obtain the existing facet's length and scale, to use as defaults if none are specified on the @Column
        // this will mean a metamodel validation exception will only be fired later (see #refineMetaModelValidator)
        // if there was an *explicit* value defined on the @Column annotation that is incompatible with existing.
        Integer existingLength = null;
        Integer existingScale = null;
        BigDecimalValueFacet existingFacet = (BigDecimalValueFacet) holder.getFacet(BigDecimalValueFacet.class);
        if(existingFacet != null && !existingFacet.isNoop()) {
            existingLength = existingFacet.getLength();
            existingScale = existingFacet.getScale();
        }
        final Column annotation = Annotations.getAnnotation(processMethodContext.getMethod(), Column.class);
        final BigDecimalValueFacet facet = annotation == null 
                ? new BigDecimalFacetFallback(holder) 
                : new BigDecimalFacetDerivedFromJdoColumn(
                        holder, 
                        valueElseDefaults(annotation.length(), existingLength, DEFAULT_LENGTH), 
                        valueElseDefaults(annotation.scale(), existingScale, DEFAULT_SCALE));
                
        // any existing BigDecimalValueFacets, eg from the @javax.validation.constraints.Digits annotation, will be
        // chained to this facet as the 'underlying'.  We check they are compatible in the #refineMetaModelValidator, below.
        FacetUtil.addFacet(facet);
    }

    private final static Integer valueElseDefaults(final int value, final Integer underlying, int defaultVal) {
        return value != -1
                ? value
                : underlying != null
                    ? underlying
                    : defaultVal;
    }


    @Override
    public void refineMetaModelValidator(MetaModelValidatorComposite metaModelValidator, IsisConfiguration configuration) {
        metaModelValidator.add(new MetaModelValidatorVisiting(newValidatorVisitor()));
    }

    private Visitor newValidatorVisitor() {
        return new MetaModelValidatorVisiting.Visitor() {

            @Override
            public boolean visit(ObjectSpecification objectSpec, ValidationFailures validationFailures) {
                validate(objectSpec, validationFailures);
                return true;
            }

            private void validate(ObjectSpecification objectSpec, ValidationFailures validationFailures) {
                
                // only consider persistent entities
                final JdoPersistenceCapableFacet pcFacet = objectSpec.getFacet(JdoPersistenceCapableFacet.class);
                if(pcFacet==null || pcFacet.getIdentityType() == IdentityType.NONDURABLE) {
                    return;
                }
                
                final List<ObjectAssociation> associations = objectSpec.getAssociations(Contributed.EXCLUDED, ObjectAssociation.Filters.PROPERTIES);
                for (ObjectAssociation association : associations) {
                    
                    // skip checks if annotated with JDO @NotPersistent
                    if(association.containsDoOpFacet(JdoNotPersistentFacet.class)) {
                        return;
                    }
                    
                    validateBigDecimalValueFacet(association, validationFailures);
                }
            }

            private void validateBigDecimalValueFacet(ObjectAssociation association, ValidationFailures validationFailures) {
                BigDecimalValueFacet facet = association.getFacet(BigDecimalValueFacet.class);
                if(facet == null) {
                    return;
                }
                
                BigDecimalValueFacet underlying = (BigDecimalValueFacet) facet.getUnderlyingFacet();
                if(underlying == null) {
                    return;
                } 
                
                if(facet instanceof BigDecimalFacetDerivedFromJdoColumn) {
                    
                    if(underlying instanceof BigDecimalFacetForPropertyFromJavaxValidationDigitsAnnotation) {

                        if(notNullButNotEqual(facet.getLength(), underlying.getLength())) {
                            validationFailures.add("%s: @javax.jdo.annotations.Column(length=...) different from @javax.validation.constraint.Digits(...); should equal the sum of its integer and fraction attributes", association.getIdentifier().toClassAndNameIdentityString());
                        }
    
                        if(notNullButNotEqual(facet.getScale(), underlying.getScale())) {
                            validationFailures.add("%s: @javax.jdo.annotations.Column(scale=...) different from @javax.validation.constraint.Digits(fraction=...)", association.getIdentifier().toClassAndNameIdentityString());
                        }
                    }
                }
            }

            private boolean notNullButNotEqual(Integer x, Integer y) {
                return x != null && y != null && !x.equals(y);
            }
        };
    }

    
}
