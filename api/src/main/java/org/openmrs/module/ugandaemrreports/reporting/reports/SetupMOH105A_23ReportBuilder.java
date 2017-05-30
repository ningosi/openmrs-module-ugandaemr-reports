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

import org.openmrs.module.reporting.dataset.definition.CohortIndicatorDataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.ugandaemrreports.library.Moh105IndicatorLibrary;
import org.openmrs.module.ugandaemrreports.reporting.library.dimension.CommonReportDimensionLibrary;
import org.openmrs.module.ugandaemrreports.reporting.utils.ColumnParameters;
import org.openmrs.module.ugandaemrreports.reporting.utils.EmrReportingUtils;
import org.openmrs.module.ugandaemrreports.reporting.utils.ReportUtils;
import org.openmrs.module.ugandaemrreports.reports.UgandaEMRDataExportManager;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Created by Nicholas Ingosi on 5/29/17.
 */
public class SetupMOH105A_23ReportBuilder extends UgandaEMRDataExportManager {

    @Autowired
    private CommonReportDimensionLibrary dimensionLibrary;

    @Autowired
    private Moh105IndicatorLibrary indicatorLibrary;

    /**
     * @return the uuid for the report design for exporting to Excel
     */
    @Override
    public String getExcelDesignUuid() {
        return "3f07996a-444c-11e7-9010-507b9dc4c741";
    }

    /**
     * Build the report design for the specified report, this allows a user to override the report design by adding properties and other metadata to the report design
     *
     * @param reportDefinition
     * @return The report design
     */
    @Override
    public ReportDesign buildReportDesign(ReportDefinition reportDefinition) {
        return createExcelTemplateDesign(getExcelDesignUuid(), reportDefinition, "105PostnatalReport.xls");
    }

    @Override
    public String getUuid() {
        return "4e2ff23e-444c-11e7-ae36-507b9dc4c741";
    }

    @Override
    public String getName() {
        return "HMIS 105 MCH - POSTNATAL";
    }

    @Override
    public String getDescription() {
        return "Health Unit Outpatient Monthly Report, Maternal and Child Health 2.3 Postnatal";
    }

    @Override
    public ReportDefinition constructReportDefinition() {

        ReportDefinition rd = new ReportDefinition();
        rd.setUuid(getUuid());
        rd.setName(getName());
        rd.setDescription(getDescription());
        rd.setParameters(getParameters());



        CohortIndicatorDataSetDefinition dsd = new CohortIndicatorDataSetDefinition();
        dsd.setParameters(getParameters());
        dsd.addDimension("age", ReportUtils.map(dimensionLibrary.standardAgeGroupsForAnc(), "onDate=${endDate}"));
        dsd.addDimension("gender", ReportUtils.map(dimensionLibrary.gender()));

        ColumnParameters female10To19 = new ColumnParameters("f10to19", "10-19", "gender=F|age=<11");
        ColumnParameters female20To24 = new ColumnParameters("f20to24", "20-24", "gender=F|age=<21");
        ColumnParameters female25Plus = new ColumnParameters("f25plus", ">=25", "gender=F|age=25+");

        List<ColumnParameters> noTotalsColumns = Arrays.asList(female10To19, female20To24, female25Plus);
        String params = "startDate=${startDate},endDate=${endDate}";
        
        //start building the columns for the report
        EmrReportingUtils.addRow(dsd, "P1-A", "P1: Post Natal Attendances - Age group", ReportUtils.map(indicatorLibrary.pncAttendances(), params), noTotalsColumns, Arrays.asList("01","02","03"));
        dsd.addColumn("P1-6H", "P1-6 Hours", ReportUtils.map(indicatorLibrary.pncAttendances("1822AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"), params), "");
        dsd.addColumn("P1-6D", "P1-6 Days", ReportUtils.map(indicatorLibrary.pncAttendances("1072AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"), params), "");
        dsd.addColumn("P1-6W", "P1-6 Weeks", ReportUtils.map(indicatorLibrary.pncAttendances("1073AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"), params), "");
        dsd.addColumn("P1-6M", "P1-6 Months", ReportUtils.map(indicatorLibrary.pncAttendances("1074AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"), params), "");

        dsd.addColumn("P2-1", "P2-1st test during postnatal - Breastfeeding mothers tested for HIV", ReportUtils.map(indicatorLibrary.pncAttendances("1074AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"), params), "");
        dsd.addColumn("P2-2", "P2-Retest during postnatal - Breastfeeding mothers tested for HIV", ReportUtils.map(indicatorLibrary.pncAttendances("1074AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"), params), "");
        dsd.addColumn("P3-1", "P3-1st test during postnatal - Breastfeeding mothers newly testing HIV+", ReportUtils.map(indicatorLibrary.pncAttendances("1074AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"), params), "");
        dsd.addColumn("P3-2", "P3-Retest test during postnatal - Breastfeeding mothers newly testing HIV+", ReportUtils.map(indicatorLibrary.pncAttendances("1074AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"), params), "");

        dsd.addColumn("P4", "Total HIV+ mothers attending postnatal", ReportUtils.map(indicatorLibrary.totaHivPositiveMothers(), params), "");
        dsd.addColumn("P5", "HIV+ women initiating ART in PNC", ReportUtils.map(indicatorLibrary.hivPositiveWomenInitiatingART(), params), "");
        dsd.addColumn("P6", "Mother-baby pairs enrolled at Mother-Baby care point", ReportUtils.map(indicatorLibrary.enrolledAtMotherBabyCarePoint(), params), "");
        dsd.addColumn("P7", "Vitamin A supplimentation given to mothers", ReportUtils.map(indicatorLibrary.hasObs("dc918618-30ab-102d-86b0-7a5022ba4115", "680f7f8d-eac6-44b4-8899-101fa2c4f873"), params), "");
        dsd.addColumn("P8", "Clients with pre-malignant condition of breast", ReportUtils.map(indicatorLibrary.hasObs("07c10f5c-17fd-4a7e-8d72-c2252f589da0", "5e416f86-aaf1-4ae4-96f0-30226b9fdd5f"), params), "");
        dsd.addColumn("P9", "Clients with pre-malignant condition of cervix", ReportUtils.map(indicatorLibrary.hasObs("d858f8cb-fe9e-4131-8d91-cd9929cc53de", "ec3a0208-0261-450a-a13b-b524e160b8fd"), params), "");



        //connect the report definition to the dsd
        rd.addDataSetDefinition("2.3-indicators", Mapped.mapStraightThrough(dsd));
        return rd;
    }

    @Override
    public String getVersion() {
        return "0.1";
    }

    @Override
    public List<Parameter> getParameters() {
        List<Parameter> l = new ArrayList<Parameter>();
        l.add(new Parameter("startDate", "Start Date", Date.class));
        l.add(new Parameter("endDate", "End Date", Date.class));
        return l;
    }
}
