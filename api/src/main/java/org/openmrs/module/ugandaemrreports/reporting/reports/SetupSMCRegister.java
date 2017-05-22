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
package org.openmrs.module.ugandaemrreports.reporting.reports;

import org.openmrs.Concept;
import org.openmrs.PatientIdentifierType;
import org.openmrs.module.metadatadeploy.MetadataUtils;
import org.openmrs.module.reporting.data.DataDefinition;
import org.openmrs.module.reporting.data.converter.DataConverter;
import org.openmrs.module.reporting.data.converter.ObjectFormatter;
import org.openmrs.module.reporting.data.patient.definition.ConvertedPatientDataDefinition;
import org.openmrs.module.reporting.data.patient.definition.PatientIdentifierDataDefinition;
import org.openmrs.module.reporting.data.person.definition.PreferredNameDataDefinition;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.PatientDataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.ugandaemrreports.data.converter.CalculationResultConverter;
import org.openmrs.module.ugandaemrreports.data.converter.FaciltyAndOutReachDataConverter;
import org.openmrs.module.ugandaemrreports.data.converter.STIDataConverter;
import org.openmrs.module.ugandaemrreports.definition.data.definition.CalculationDataDefinition;
import org.openmrs.module.ugandaemrreports.library.Cohorts;
import org.openmrs.module.ugandaemrreports.library.DataFactory;
import org.openmrs.module.ugandaemrreports.reporting.calculation.smc.AgeFromEncounterDateCalculation;
import org.openmrs.module.ugandaemrreports.reporting.calculation.smc.SMCAdrressCalculation;
import org.openmrs.module.ugandaemrreports.reporting.calculation.smc.SMCEncounterDateCalculation;
import org.openmrs.module.ugandaemrreports.reporting.dataset.definition.SharedDataDefintion;
import org.openmrs.module.ugandaemrreports.reporting.metadata.Dictionary;
import org.openmrs.module.ugandaemrreports.reports.UgandaEMRDataExportManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

/**
 * Created by Nicholas Ingosi on 5/17/17.
 */
@Component
public class SetupSMCRegister extends UgandaEMRDataExportManager {
    @Autowired
    private DataFactory df;

    @Autowired
    SharedDataDefintion sdd;
    /**
     * @return the uuid for the report design for exporting to Excel
     */
    @Override
    public String getExcelDesignUuid() {
        return "7def95f0-3b38-11e7-b8de-507b9dc4c741";
    }

    @Override
    public List<ReportDesign> constructReportDesigns(ReportDefinition reportDefinition) {
        List<ReportDesign> l = new ArrayList<ReportDesign>();
        l.add(buildReportDesign(reportDefinition));
        return l;
    }

    /**
     * Build the report design for the specified report, this allows a user to override the report design by adding properties and other metadata to the report design
     *
     * @param reportDefinition
     * @return The report design
     */
    @Override
    public ReportDesign buildReportDesign(ReportDefinition reportDefinition) {
        ReportDesign rd = createExcelTemplateDesign(getExcelDesignUuid(), reportDefinition, "SMCRegister.xls");
        Properties props = new Properties();
        props.put("repeatingSections", "sheet:1,row:8,dataset:SMC-DSD");
        props.put("sortWeight", "5000");
        rd.setProperties(props);
        return rd;
    }

    @Override
    public String getUuid() {
        return "c1122646-3b37-11e7-aa22-507b9dc4c741";
    }

    @Override
    public String getName() {
        return "Safe Male Circumcision Register";
    }

    @Override
    public String getDescription() {
        return "Safe Male Circumcision Register";
    }

    @Override
    public ReportDefinition constructReportDefinition() {
        ReportDefinition rd = new ReportDefinition();

        rd.setUuid(getUuid());
        rd.setName(getName());
        rd.setDescription(getDescription());
        rd.addParameters(getParameters());
        rd.addDataSetDefinition("SMC-DSD", Mapped.mapStraightThrough(dataSetDefinition()));
        return rd;
    }

    @Override
    public String getVersion() {
        return "0.1";
    }

    @Override
    public List<Parameter> getParameters() {
        List<Parameter> l = new ArrayList<Parameter>();
        l.add(df.getStartDateParameter());
        l.add(df.getEndDateParameter());
        return l;
    }

    private DataSetDefinition dataSetDefinition() {
        PatientDataSetDefinition dsd = new PatientDataSetDefinition();
        dsd.setName(getName());
        dsd.addParameters(getParameters());
        dsd.addRowFilter(Cohorts.genderAndHasAncEncounter(false, true, "244da86d-f80e-48fe-aba9-067f241905ee"), "startDate=${startDate},endDate=${endDate}");

        PatientIdentifierType serialNo= MetadataUtils.existing(PatientIdentifierType.class, "37601abe-2ee0-4493-8ac7-22b4972190cf");
        DataConverter identifierFormatter = new ObjectFormatter("{identifier}");
        DataDefinition identifierDef = new ConvertedPatientDataDefinition("identifier", new PatientIdentifierDataDefinition(serialNo.getName(), serialNo), identifierFormatter);

        dsd.addColumn("Date", getEncounterDate(), "onOrBefore=${endDate}", new CalculationResultConverter());
        dsd.addColumn("Serial No", identifierDef, "");
        dsd.addColumn("Names of Client", new PreferredNameDataDefinition(), (String) null);
        dsd.addColumn("Age<2yrs", getAgeFromEncounterDate(0, 2), "onOrBefore=${endDate}", new CalculationResultConverter());
        dsd.addColumn("Age2<5yrs", getAgeFromEncounterDate(2, 5), "onOrBefore=${endDate}", new CalculationResultConverter());
        dsd.addColumn("Age5<15yrs", getAgeFromEncounterDate(5, 15), "onOrBefore=${endDate}", new CalculationResultConverter());
        dsd.addColumn("Age15<49yrs", getAgeFromEncounterDate(15, 49), "onOrBefore=${endDate}", new CalculationResultConverter());
        dsd.addColumn("Age<49yrs", getAgeFromEncounterDate(49, 200), "onOrBefore=${endDate}", new CalculationResultConverter());
        dsd.addColumn("Address", address(), "onOrBefore=${endDate}", new CalculationResultConverter());
        dsd.addColumn("Facility/Outreach", sdd.definition("Facility/Outreach", getConcept("ac44b5f2-cf57-43ca-bea0-8b392fe21802")), "onOrAfter=${startDate},onOrBefore=${endDate}", new FaciltyAndOutReachDataConverter());
        dsd.addColumn("STI", sdd.definition("STI", getConcept("")), "onOrAfter=${startDate},onOrBefore=${endDate}", new STIDataConverter());

        return dsd;
    }

    private DataDefinition getEncounterDate() {
        CalculationDataDefinition cd = new CalculationDataDefinition("Date", new SMCEncounterDateCalculation());
        cd.addParameter(new Parameter("onDate", "On Date", Date.class));
        return cd;
    }

    private DataDefinition getAgeFromEncounterDate(Integer lower, Integer upper) {
        CalculationDataDefinition cd = new CalculationDataDefinition("Date", new AgeFromEncounterDateCalculation());
        cd.addParameter(new Parameter("onDate", "On Date", Date.class));
        cd.addCalculationParameter("lower", lower);
        cd.addCalculationParameter("upper", upper);
        return cd;
    }
    private DataDefinition address() {
        CalculationDataDefinition cd = new CalculationDataDefinition("address", new SMCAdrressCalculation());
        cd.addParameter(new Parameter("onDate", "On Date", Date.class));
        return cd;

    }

    private Concept getConcept(String uuid) {
        return Dictionary.getConcept(uuid);
    }
}
