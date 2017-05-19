/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.ugandaemrreports.data.converter;

import org.openmrs.Encounter;
import org.openmrs.module.reporting.data.converter.DataConverter;

import java.util.Date;
import java.util.List;

/**
 * Created by Nicholas Ingosi on 5/18/17.
 */
public class EncounterDataDefinitionConverter implements DataConverter {
    @Override
    public Object convert(Object obj) {

        if (obj == null) {
            return "";
        }
        List<Encounter> encounters = (List<Encounter>)obj;
        if(encounters.size() > 0){
            return encounters.get(0).getEncounterDatetime();
        }

        return null;
    }



    @Override
    public Class<?> getInputDataType() {
        return Encounter.class;
    }

    @Override
    public Class<?> getDataType() {
        return Date.class;
    }
}
