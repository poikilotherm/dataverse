package edu.harvard.iq.dataverse.flyway;

import edu.harvard.iq.dataverse.DataverseServiceBean;
import org.flywaydb.core.api.callback.Callback;
import org.flywaydb.core.api.callback.Context;
import org.flywaydb.core.api.callback.Event;

import javax.ejb.EJB;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BootstrapCallback implements Callback {
  
    private static final Logger logger = Logger.getLogger(BootstrapCallback.class.getCanonicalName());
    
    @EJB
    protected DataverseServiceBean dataverseServiceBean;
  
    @Override
    /**
     * We only support AFTER_MIGRATE event for bootstrapping.
     */
    public boolean supports(Event event, Context context) {
        return event == Event.AFTER_MIGRATE;
    }
    
    @Override
    /**
     * Bootstrapping should be done in a transaction...
     */
    public boolean canHandleInTransaction(Event event, Context context) {
        return true;
    }
  
    @Override
    public void handle(Event event, Context context) {
        // Be sure to only handle the right events.
        if (event != Event.AFTER_MIGRATE) {
          logger.log(Level.INFO, "Bootstrapping was called with event type '"+event.toString()+"'. Skipping.");
          return;
        }
        // Only run if no root dataverse exists.
        if (! dataverseServiceBean.isRootDataverseExists()) {
          logger.log(Level.INFO, "Skipping bootstrapping as Root Dataverse exists.");
          return;
        }
        logger.log(Level.INFO, "Bootstrapping follows. Not yet implemented.");
    }
  
  /*
  Logger log = Logger.getLogger(FillDatabaseAfterMigrate.class.getSimpleName());
  
  @Override
  public void afterMigrate(Connection connection) {
    log.info("afterMigrate");
    Statement st;
    try {
      st = connection.createStatement();
      ResultSet rs = st.executeQuery("SELECT count(id) FROM book");
      rs.next();
      if (rs.getInt(1) == 0) {
        st.execute("INSERT INTO author (id, firstname, lastname) VALUES ((SELECT nextval('author_seq')), 'Thorben', 'Janssen');");
        st.execute("INSERT INTO book (id, publishingdate, title, fk_author, price) VALUES ((SELECT nextval('book_seq')), '2017-04-04', 'Hibernate Tips - More than 70 solutions to common Hibernate problems', 1, 9.99);");
        log.info("Database was empty. Added example data.");
      } else {
        log.info("Database contains books. No example data needed.");
        return;
      }
    } catch (SQLException e) {
      throw new MigrationException(e);
    }
  }
  */
}