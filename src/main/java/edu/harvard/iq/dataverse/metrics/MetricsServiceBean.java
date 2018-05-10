package edu.harvard.iq.dataverse.metrics;

import edu.harvard.iq.dataverse.Metric;
import edu.harvard.iq.dataverse.util.SystemConfig;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NonUniqueResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

@Stateless
public class MetricsServiceBean implements Serializable {

    private static final Logger logger = Logger.getLogger(MetricsServiceBean.class.getCanonicalName());

    private static final SimpleDateFormat yyyymmFormat = new SimpleDateFormat(MetricsUtil.YEAR_AND_MONTH_PATTERN);    
    
    @PersistenceContext(unitName = "VDCNet-ejbPU")
    private EntityManager em;
    @EJB
    SystemConfig systemConfig;    
    
    //This is for deciding whether to used a cached value on monthly queries
    //Assumes the metric passed in is sane (e.g. not run for past the current month, not a garbled date string, etc)
    public boolean doWeQueryAgainMonthly(Metric queriedMetric) {
        if(null == queriedMetric) { //never queried before
            return true;
        }
        
        String yyyymm= queriedMetric.getMetricDateString();
        String thisMonthYYYYMM = MetricsUtil.getCurrentMonth();
        
        Date lastCalled = queriedMetric.getLastCalledDate();
        LocalDateTime ldt = LocalDateTime.ofInstant((new Date()).toInstant(), ZoneId.systemDefault());
                
        int minutesUntilNextQuery = systemConfig.getMetricsCacheTimeoutMinutes();
        
        if( yyyymm.equals(thisMonthYYYYMM) ) { //if this month
            LocalDateTime ldtMinus = ldt.minusMinutes(minutesUntilNextQuery) ;
            Date todayMinus = Date.from(ldtMinus.atZone(ZoneId.systemDefault()).toInstant());
            
            //allow if today minus query wait is after last called time
            return (todayMinus.after(lastCalled)); 
        } else {
            String lastRunYYYYMM = yyyymmFormat.format(lastCalled);
            
            //if queried was last run during the month it was querying.  
            //Allows one requery of a past month to make it up to date.
            return (lastRunYYYYMM.equals(yyyymm)); 
        }
    }
    
    //MAD: MAYBE THIS LOGIC CAN BE USED ABOVE TO STOP REPEATED CODE?
    //This is for deciding whether to used a cached value over all time
    public boolean doWeQueryAgainAllTime(Metric queriedMetric) {
        if(null == queriedMetric) { //never queried before
            return true;
        }
        int minutesUntilNextQuery = systemConfig.getMetricsCacheTimeoutMinutes();
        Date lastCalled = queriedMetric.getLastCalledDate();
        LocalDateTime ldt = LocalDateTime.ofInstant((new Date()).toInstant(), ZoneId.systemDefault());
        
        LocalDateTime ldtMinus = ldt.minusMinutes(minutesUntilNextQuery) ;
        Date todayMinus = Date.from(ldtMinus.atZone(ZoneId.systemDefault()).toInstant());

        //allow if today minus query wait is after last called time
        return (todayMinus.after(lastCalled)); 
    }

//MAD: THIS SHOULD DO ITS OWN NULL CHECK
//    public boolean doWeQueryAgainMonthly(Metric queriedMetric) {
//        
//        String title= queriedMetric.getMetricTitle();
//        String yyyymm= queriedMetric.getMetricDateString();
//        String thisMonthYYYYMM = MetricsUtil.getCurrentMonth();
//        
//        int minutesUntilNextQuery = systemConfig.getMetricsCacheTimeoutMinutes();
//        
//        //MAD: What do I actually think the (a?) issue is:
//        //This only allows requery if the cached value is in the current month.
//        //Instead, we want to allow re-query also if a query has not been run AFTER the month in question
//
//    
//        String lastRunYYYYMM = yyyymmFormat.format(queriedMetric.getLastCalledDate()); //MAD: We DON"T WANT A STRING, WE NEED TO COMPARE DATES. I feel like this is more complicated than it should be...
//        
//        //I'm pretty sure this also misses a case where the date rolls over to the next month or year
//        //MAD: First if does not take account of what happens if no previous, but then again I think this method is only called when there is a previous passed in...
//        logger.info("Current yyyymm: " + thisMonthYYYYMM + " Last query yyyymm: " + yyyymm);
//        if( yyyymm.equals(thisMonthYYYYMM) ) { //if this month
//            LocalDateTime ldtMinus = LocalDateTime.ofInstant((new Date()).toInstant(), ZoneId.systemDefault()).minusMinutes(minutesUntilNextQuery) ;
//            Date todayMinus = Date.from(ldtMinus.atZone(ZoneId.systemDefault()).toInstant());
//            Date lastCalled = queriedMetric.getLastCalledDate();
//            logger.info("Query allowed. Title: " + title + " yyyymm: " + yyyymm);
//            
//            // || (!yyyymm.equals(thisMonthYYYYMM) && lastCalled.getMonth() 
//            
//            return (todayMinus.after(lastCalled)); 
//        } else {
//            //We do not allow queries of previous months.
//            //MAD: I bet this also messes up with the rollover. We don't want a month with incomplete data to be never queried again.
//            logger.info("Query denied. Title: " + title + " yyyymm: " + yyyymm);
//            return false;
//        }
//    }
    
    public Metric save(Metric newMetric, boolean monthly) throws Exception {
        Metric oldMetric;
        if(monthly) {
            oldMetric = getMetric(newMetric.getMetricTitle(), newMetric.getMetricDateString());
        } else {
            oldMetric = getMetric(newMetric.getMetricTitle());
        }
        if(oldMetric != null) {
            em.remove(oldMetric);
            em.flush();
        }
        em.persist(newMetric);
        return em.merge(newMetric);

    }
    
    public Metric getMetric(String metricTitle, String yymmmm) throws Exception {
        String searchMetricName = Metric.generateMetricName(metricTitle, yymmmm);
        
        return getMetric(searchMetricName);
        
    }
    
    public Metric getMetric(String searchMetricName) throws Exception{
        Query query = em.createQuery("select object(o) from Metric as o where o.metricName = :metricName", Metric.class);
        query.setParameter("metricName", searchMetricName);
        Metric metric = null;
        try {
            metric = (Metric) query.getSingleResult();
        } catch (javax.persistence.NoResultException nr){
            //do nothing
        }  catch (NonUniqueResultException nur) {
            throw new Exception("Multiple cached results found for this query. Contact your system administrator.");
        }
        return metric;
    }
    
    /**
     * @param yyyymm Month in YYYY-MM format.
     */
    public long dataversesByMonth(String yyyymm) throws Exception{
        String sanitizedyyyymm = MetricsUtil.sanitizeYearMonthUserInput(yyyymm); //MAD: THIS SHOULD CHECK INPUT OK I THINK
        String metricName = "dataversesByMonth";
        Metric queriedMetric = getMetric(metricName,sanitizedyyyymm);
        
        Long result;
        if(!doWeQueryAgainMonthly(queriedMetric)) {
            return Long.parseLong(queriedMetric.getMetricValue());
        }
        Query query = em.createNativeQuery(""
            + "select count(dvobject.id)\n"
            + "from dataverse\n"
            + "join dvobject on dvobject.id = dataverse.id\n"
            + "where dvobject.publicationdate is not null\n"
            + "and date_trunc('month', publicationdate) <=  to_date('" + sanitizedyyyymm + "','YYYY-MM');"
        );
        result = (long) query.getSingleResult();
        save(new Metric(metricName,yyyymm, result.toString()), true);
        
        return result;
    }

    /**
     * @param yyyymm Month in YYYY-MM format.
     */
    public long datasetsByMonth(String yyyymm) throws Exception{
        String sanitizedyyyymm = MetricsUtil.sanitizeYearMonthUserInput(yyyymm);
        Query query = em.createNativeQuery(""
                + "select count(*)\n"
                + "from datasetversion\n"
                + "where concat(datasetversion.dataset_id,':', datasetversion.versionnumber + (.1 * datasetversion.minorversionnumber)) in \n"
                + "(\n"
                + "select concat(datasetversion.dataset_id,':', max(datasetversion.versionnumber + (.1 * datasetversion.minorversionnumber))) as max \n"
                + "from datasetversion\n"
                + "join dataset on dataset.id = datasetversion.dataset_id\n"
                + "where versionstate='RELEASED' \n"
                + "and date_trunc('month', releasetime) <=  to_date('" + sanitizedyyyymm + "','YYYY-MM')\n"
                + "and dataset.harvestingclient_id is null\n"
                + "group by dataset_id \n"
                + ");"
        );
        logger.fine("query: " + query);
        return (long) query.getSingleResult();
    }

    /**
     * @param yyyymm Month in YYYY-MM format.
     */
    public long filesByMonth(String yyyymm) throws Exception{
        String sanitizedyyyymm = MetricsUtil.sanitizeYearMonthUserInput(yyyymm);
        Query query = em.createNativeQuery(""
                + "select count(*)\n"
                + "from filemetadata\n"
                + "join datasetversion on datasetversion.id = filemetadata.datasetversion_id\n"
                + "where concat(datasetversion.dataset_id,':', datasetversion.versionnumber + (.1 * datasetversion.minorversionnumber)) in \n"
                + "(\n"
                + "select concat(datasetversion.dataset_id,':', max(datasetversion.versionnumber + (.1 * datasetversion.minorversionnumber))) as max \n"
                + "from datasetversion\n"
                + "join dataset on dataset.id = datasetversion.dataset_id\n"
                + "where versionstate='RELEASED'\n"
                //                + "and date_trunc('month', releasetime) <=  to_date('2018-03','YYYY-MM')\n"
                // FIXME: Remove SQL injection vector: https://software-security.sans.org/developer-how-to/fix-sql-injection-in-java-persistence-api-jpa
                + "and date_trunc('month', releasetime) <=  to_date('" + sanitizedyyyymm + "','YYYY-MM')\n"
                + "and dataset.harvestingclient_id is null\n"
                + "group by dataset_id \n"
                + ");"
        );
        logger.fine("query: " + query);
        return (long) query.getSingleResult();
    }

    /**
     * @param yyyymm Month in YYYY-MM format.
     */
    public long downloadsByMonth(String yyyymm) throws Exception{
        String sanitizedyyyymm = MetricsUtil.sanitizeYearMonthUserInput(yyyymm);
        Query query = em.createNativeQuery(""
                + "select count(id)\n"
                + "from guestbookresponse\n"
                + "where date_trunc('month', responsetime) <=  to_date('" + sanitizedyyyymm + "','YYYY-MM');"
        );
        logger.fine("query: " + query);
        return (long) query.getSingleResult();
    }

    
//        /**
//     * @param yyyymm Month in YYYY-MM format.
//     */
//    public long dataversesByMonth(String yyyymm) throws Exception{
//        String sanitizedyyyymm = MetricsUtil.sanitizeYearMonthUserInput(yyyymm); //MAD: THIS SHOULD CHECK INPUT OK I THINK
//        String metricName = "dataversesByMonth";
//        Metric queriedMetric = getMetric(metricName,sanitizedyyyymm);
//        
//        Long result;
//        if(!doWeQueryAgainMonthly(queriedMetric)) {
//            return queriedMetric.getMetricValue();
//        }
//        Query query = em.createNativeQuery(""
//            + "select count(dvobject.id)\n"
//            + "from dataverse\n"
//            + "join dvobject on dvobject.id = dataverse.id\n"
//            + "where dvobject.publicationdate is not null\n"
//            + "and date_trunc('month', publicationdate) <=  to_date('" + sanitizedyyyymm + "','YYYY-MM');"
//        );
//        result = (long) query.getSingleResult();
//        save(new Metric(metricName,yyyymm,result));
//        
//        return result;
//    }
    
    public List<Object[]> dataversesByCategory() throws Exception {
        String metricName = "dataversesByCategory";
        Metric queriedMetric = getMetric(metricName);
        
        if(!doWeQueryAgainAllTime(queriedMetric)) {
            String cachedMetricString = queriedMetric.getMetricValue();
            
            return null; //queriedMetric.getMetricValue(); //MAD: FIX ME!
        }
        
        Query query = em.createNativeQuery(""
                + "select dataversetype, count(dataversetype) from dataverse\n"
                + "join dvobject on dvobject.id = dataverse.id\n"
                + "where dvobject.publicationdate is not null\n"
                + "group by dataversetype\n"
                + "order by count desc;"
        );
        
        //MAD: collapse these after debugging
        List<Object[]> returnList = query.getResultList(); 
        return returnList;
    }

    public List<Object[]> datasetsBySubject() {
        Query query = em.createNativeQuery(""
                + "SELECT strvalue, count(dataset.id)\n"
                + "FROM datasetfield_controlledvocabularyvalue \n"
                + "JOIN controlledvocabularyvalue ON controlledvocabularyvalue.id = datasetfield_controlledvocabularyvalue.controlledvocabularyvalues_id\n"
                + "JOIN datasetfield ON datasetfield.id = datasetfield_controlledvocabularyvalue.datasetfield_id\n"
                + "JOIN datasetfieldtype ON datasetfieldtype.id = controlledvocabularyvalue.datasetfieldtype_id\n"
                + "JOIN datasetversion ON datasetversion.id = datasetfield.datasetversion_id\n"
                + "JOIN dvobject ON dvobject.id = datasetversion.dataset_id\n"
                + "JOIN dataset ON dataset.id = datasetversion.dataset_id\n"
                + "WHERE\n"
                + "datasetfieldtype.name = 'subject'\n"
                + "AND dvobject.publicationdate is NOT NULL\n"
                + "AND dataset.harvestingclient_id IS NULL\n"
                + "GROUP BY strvalue\n"
                + "ORDER BY count(dataset.id) desc;"
        );
        logger.info("query: " + query);
        
        //MAD: collapse these after debugging
        List<Object[]> returnList = query.getResultList(); 
        return returnList;
        
    }

}
