package edu.eci.Punto2_BlackList.blacklistvalidator;

import edu.eci.arsw.spamkeywordsdatasource.HostBlacklistsDataSourceFacade;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class BlackListSearchWorker extends Thread {

    private final HostBlacklistsDataSourceFacade facade;
    private final String host;
    private final int startIndex;
    private final int endIndex;
    private final AtomicInteger sharedOccurrencesCount;
    private final int alarmCount;

    private final List<Integer> foundInLists = new ArrayList<>();
    private int checkedCount = 0;

    public BlackListSearchWorker(HostBlacklistsDataSourceFacade facade, String host,
                                 int startIndex, int endIndex,
                                 AtomicInteger sharedOccurrencesCount, int alarmCount) {
        this.facade = facade;
        this.host = host;
        this.startIndex = startIndex;
        this.endIndex = endIndex;
        this.sharedOccurrencesCount = sharedOccurrencesCount;
        this.alarmCount = alarmCount;
    }

    @Override
    public void run() {
        for (int i = startIndex; i < endIndex; i++) {
            if (sharedOccurrencesCount.get() >= alarmCount) {
                break;
            }
            checkedCount++;
            if (facade.isInBlackListServer(i, host)) {
                foundInLists.add(i);
                sharedOccurrencesCount.incrementAndGet();
            }
        }
    }

    public int getOcurrenceCount() {
        return foundInLists.size();
    }

    public List<Integer> getFoundInLists() {
        return foundInLists;
    }

    public int getSegmentSize() {
        return endIndex - startIndex;
    }

    public int getCheckedCount() {
        return checkedCount;
    }
}