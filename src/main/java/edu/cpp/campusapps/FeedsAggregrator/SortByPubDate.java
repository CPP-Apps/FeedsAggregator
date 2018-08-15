package edu.cpp.campusapps.FeedsAggregrator;

import com.rometools.rome.feed.synd.SyndEntry;
import java.util.Comparator;

public class SortByPubDate implements Comparator<SyndEntry> {

    @Override
    public int compare(SyndEntry syndEntry, SyndEntry t1) {
        return syndEntry.getPublishedDate().compareTo(t1.getPublishedDate());
    }

}
