package com.objectmentor.library.offline;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.objectmentor.library.application.gateways.BookGateway;
import com.objectmentor.library.application.gateways.CompactDiscGateway;
import com.objectmentor.library.application.models.Book;
import com.objectmentor.library.application.models.CompactDisc;
import com.objectmentor.library.application.models.LoanReceipt;
import com.objectmentor.library.application.models.Media;
import com.objectmentor.library.application.models.MediaCopy;

public class InMemoryMediaGateway implements BookGateway, CompactDiscGateway {

  private Map mediaCopies = new HashMap();
  private long lastCopyId = 0;

  public InMemoryMediaGateway() {
    super();
  }

  public MediaCopy addCopy(Media media) {
    lastCopyId += 1;
    MediaCopy mediaCopy = new MediaCopy(media, "" + lastCopyId);
    String mediaId = media.getId();
    List copies = (List) mediaCopies.get(mediaId);
    if (copies == null) {
      copies = new LinkedList();
      mediaCopies.put(mediaId, copies);
    }
    copies.add(mediaCopy);
    return mediaCopy;
  }

  protected String getLastId() {
    return "" + lastCopyId;
  }

  public List addCopies(Media media, int numberOfNewCopies) {
    ArrayList list = new ArrayList();
    for (int i = 0; i < numberOfNewCopies; i++)
      list.add(addCopy(media));
    return list;
  }

  public List findAllLoanReceiptsFor(String patronId) {
    List receipts = new LinkedList();
    Collection copyLists = mediaCopies.values();
    for (Iterator i = copyLists.iterator(); i.hasNext();) {
      List copyList = (List) i.next();
      for (int j = 0; j < copyList.size(); j++) {
        MediaCopy mediaCopy = (MediaCopy) copyList.get(j);
        LoanReceipt receipt = mediaCopy.getLoanReceipt();
        if (receipt != null && receipt.getBorrower().getId().equals(patronId))
          receipts.add(receipt);
      }
    }
    return receipts;
  }

  public void clear() {
    lastCopyId = 0;
    mediaCopies.clear();
  }

  public int mediaCount() {
    return mediaCopies.size();
  }

  public List findAllCopies(String isbn) {
    List copies = (List) this.mediaCopies.get(isbn);
    if (copies == null)
      return new ArrayList();
    return copies;
  }

  public boolean contains(String id) {
    return mediaCopies.containsKey(id);
  }

  public MediaCopy findAvailableCopy(String id) {
    List copies = findAllCopies(id);
    for (int i = 0; i < copies.size(); i++) {
      MediaCopy copy = (MediaCopy) copies.get(i);
      if (!copy.isLoaned())
        return copy;
    }
    return null;
  }

  public MediaCopy findCopyById(String copyId) {
    Collection listsOfCopies = mediaCopies.values();
    for (Iterator i = listsOfCopies.iterator(); i.hasNext();) {
      List copies = (List) i.next();
      for (int j = 0; j < copies.size(); j++) {
        MediaCopy mediaCopy = (MediaCopy) copies.get(j);
        if (mediaCopy.getId().equals(copyId))
          return mediaCopy;
      }
    }
    return null;
  }

  public void delete(MediaCopy copy) {
    for (Iterator iter = mediaCopies.values().iterator(); iter.hasNext();) {
      List list = (List) iter.next();
      if (list.remove(copy)) {
        return;
      }
    }
  }

  public List findAllISBNs() {
    return findAllKeysForValuesOfType(Book.class);
  }

  public int copyCount() {
    int count = 0;
    for (Iterator iter = mediaCopies.values().iterator(); iter.hasNext();) {
      List list = (List) iter.next();
      count += list.size();
    }
    return count;
  }

  public Map findAllISBNsAndTitles() {
    List isbns = findAllKeysForValuesOfType(Book.class);
    Map map = new HashMap();
    for (Iterator iter = isbns.iterator(); iter.hasNext();) {
      String isbn = (String) iter.next();
      List copies = (List) mediaCopies.get(isbn);
      if (copies.size() > 0) {
        String title = ((MediaCopy) copies.get(0)).getMedia().getTitle();
        map.put(isbn, title);
      }
    }
    return map;
  }

  public List findAllCDs() {
    return findAllCollectionsOfType(CompactDisc.class);
  }

  private List findAllKeysForValuesOfType(Class type) {
    List list = new ArrayList();
    for (Iterator iter = mediaCopies.keySet().iterator(); iter.hasNext();) {
      String key = (String) iter.next();
      Media media = getFirstInList((List) mediaCopies.get(key));
      if (media != null && media.getClass().equals(type))
        list.add(key);
    }
    return list;
  }

  private List findAllCollectionsOfType(Class type) {
    List list = new ArrayList();
    List keys = findAllKeysForValuesOfType(type);
    for (Iterator iter = keys.iterator(); iter.hasNext();) {
      String key = (String) iter.next();
      list.add(mediaCopies.get(key));
    }
    return list;
  }

  private Media getFirstInList(List list) {
    return list.size() > 0 ? ((MediaCopy) list.get(0)).getMedia() : null;
  }

}