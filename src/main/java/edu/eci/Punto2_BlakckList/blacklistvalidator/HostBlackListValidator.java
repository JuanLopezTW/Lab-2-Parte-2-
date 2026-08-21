package edu.eci.Punto2_BlackList.blacklistvalidator;

import edu.eci.arsw.spamkeywordsdatasource.HostBlacklistsDataSourceFacade;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 *
 * @author hcadavid
 */
public class HostBlackListsValidator {

    private static final Logger logger = Logger.getLogger(HostBlackListsValidator.class.getName());
    private static final int BLACK_LIST_ALARM_COUNT=5;
    private final HostBlacklistsDataSourceFacade facade;

    public HostBlackListsValidator(HostBlacklistsDataSourceFacade facade){
        this.facade = facade;
    }

    /**
     * Check the given host's IP address in all the available black lists,
     * and report it as NOT Trustworthy when such IP was reported in at least
     * BLACK_LIST_ALARM_COUNT lists, or as Trustworthy in any other case.
     * The search is not exhaustive: When the number of occurrences is equal to
     * BLACK_LIST_ALARM_COUNT, the search is finished, the host reported as
     * NOT Trustworthy, and the list of the five blacklists returned.
     * @param ipaddress suspicious host's IP address.
     * @return  Blacklists numbers where the given host's IP address was found.
     * @param N    número de hilos a utilizar
     * @return lista de números de listas negras donde se encontró el host
     */
    public List<Integer> checkHost(String ipaddress, int N){

        int totalBlackLists = facade.getRegisteredServersCount();

        if (N <= 0) {
            throw new IllegalArgumentException("N debe ser mayor a 0");
        }
        if (N > totalBlackLists) {
            N = totalBlackLists;
        }

        BlackListSearchWorker[] workers = new BlackListSearchWorker[N];
        AtomicInteger sharedOccurrencesCount = new AtomicInteger(0);

        int baseSize = totalBlackLists / N;
        int remainder = totalBlackLists % N;

        int currentIndex = 0;
        for (int i = 0; i < N; i++) {
            int segmentSize = baseSize + (i < remainder ? 1 : 0);
            int start = currentIndex;
            int end = start + segmentSize;
            currentIndex = end;

            workers[i] = new BlackListSearchWorker(facade, ipaddress, start, end,
                    sharedOccurrencesCount, BLACK_LIST_ALARM_COUNT);
        }

        for (BlackListSearchWorker worker : workers) {
            worker.start();
        }

        for (BlackListSearchWorker worker : workers) {
            try {
                worker.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Búsqueda interrumpida", e);
            }
        }

        List<Integer> foundLists = new ArrayList<>();
        int totalOccurrences = 0;
        int totalChecked = 0;

        for (BlackListSearchWorker worker : workers) {
            foundLists.addAll(worker.getFoundInLists());
            totalOccurrences += worker.getOcurrenceCount();
            totalChecked += worker.getCheckedCount();
        }

        logger.info("Checked " + totalChecked + " of " + totalBlackLists + " blacklists");

        if (totalOccurrences >= BLACK_LIST_ALARM_COUNT) {
            facade.reportAsNotTrustworthy(ipaddress);
        } else {
            facade.reportAsTrustworthy(ipaddress);
        }

        return foundLists;
    }


    private static final Logger LOG = Logger.getLogger(HostBlackListsValidator.class.getName());



}
