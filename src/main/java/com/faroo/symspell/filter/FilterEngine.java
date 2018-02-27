package com.faroo.symspell.filter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import com.faroo.symspell.Verbosity;
import com.faroo.symspell.impl.v3.SymSpellV3;
import com.faroo.test.unit.TestData;
import com.google.common.collect.Multimap;
import com.google.common.collect.TreeMultimap;
import com.ibm.icu.text.Transliterator;

public class FilterEngine {

    private final SymSpellV3 matcher;
    private int editDistance;

    private final Map<String, FilterIndexEntry> nameToId = new HashMap<>();

    private final String id = "Any-Latin; NFD; [^\\p{Alnum}] Remove";

    private final Transliterator ts = Transliterator.getInstance(id);

    private final int D1;
    private final int D2;

    public FilterEngine(int editDistance) {
        this(editDistance, Verbosity.All);
    }

    public FilterEngine(int editDistance, Verbosity verbose) {
        this.editDistance = editDistance;
        matcher = new SymSpellV3(this.editDistance, verbose);
        D1 = Math.min(editDistance, 1);
        D2 = Math.min(editDistance, 2);
        this.load();
    }

    public List<FilterItem> lookup(String input) {
        return lookup(input, this.editDistance);
    }

    public List<FilterItem> lookup(String inp, int editDistance2) {
        final String input = ts.transform(inp);
        return matcher.lookup(input, editDistance2)//
                .parallelStream()//
                .map(i -> new FilterItem(input, nameToId.get(i.term).getIdList(), i.term, i.distance, i.count)) //
                .collect(Collectors.toList());
    }

    private int distance(String in) {
        switch (in.length()) {
        case 0:
        case 1:
            return 0;
        case 2:
        case 3:
            return D1;
        case 4:
        case 5:
            return D2;
        default:
            return this.editDistance;
        }
    }

    public List<Entry<Long, Map<String, Collection<Match>>>> filter(String... inputs) {
        if (inputs != null) {
            Set<String> in = new HashSet<>();
            for (String i : inputs) {
                if ((i != null) && !i.trim().isEmpty()) {
                    in.add(i.trim().toLowerCase());
                }
            }
            if (!in.isEmpty()) {
                List<List<FilterItem>> matches = in.parallelStream()//
                        .map(inp -> lookup(inp, distance(inp)))//
                        .filter(f -> f != null) //
                        .collect(Collectors.toList());
                if (!matches.isEmpty()) {
                    return merge(matches);
                }
            }

        }
        return null;
    }

    private List<Entry<Long, Map<String, Collection<Match>>>> merge(List<List<FilterItem>> lists) {
        final Multimap<Long, FilterItem> mapped = TreeMultimap.create();

        for (List<FilterItem> list : lists) {
            if (list != null) {
                list.stream().forEach(f -> {
                    f.ids.forEach(i -> mapped.put(i, f));
                });
            }
        }

        Map<Long, Map<String, Collection<Match>>> perId = new HashMap<>();
        for (Long e : mapped.keySet()) {
            Map<String, Collection<Match>> matches = new HashMap<>();
            Collection<FilterItem> items = mapped.get(e);
            perId.put(e, matches);

            for (FilterItem fi : items) {
                Collection<Match> mm = matches.get(fi.input);
                if (mm == null) {
                    mm = new HashSet<>();
                    matches.put(fi.input, mm);
                }
                mm.add(new Match(fi.term, fi.distance, fi.count));
            }
        }

        List<Entry<Long, Map<String, Collection<Match>>>> merged = perId.entrySet().stream() //
                .filter(e -> e.getValue().size() >= 2) // at least 2
                .sorted((e1, e2) -> Integer.compare(e1.getValue().size(), e2.getValue().size()))//
                .collect(Collectors.toList());

        merged = merged.stream()//
                .filter(e -> {
                    Set<String> empty = new HashSet<>();
                    for (Entry<String, Collection<Match>> xe : e.getValue().entrySet()) {
                        xe.setValue(xe.getValue().stream()//
                                .filter(m -> {
                                    // '' filter by similarity;
                                    return true;
                                })//
                                .collect(Collectors.toList()));
                        if (xe.getValue().isEmpty()) {
                            empty.add(xe.getKey());
                        }
                    }
                    empty.forEach(em -> {
                        e.getValue().remove(em);
                    });
                    return !e.getValue().isEmpty();
                }) //
                .filter(e -> e.getValue().size() >= 2) //
                .collect(Collectors.toList());
        return merged.stream()//
                .filter(e -> e.getValue().size() >= 2)//
                .collect(Collectors.toList());
    }

    private void addEntry(String name, String id) {
        try {
            name = ts.transform(name).toLowerCase();
            Long xid = Long.valueOf(Long.parseLong(id));
            FilterIndexEntry entry = nameToId.get(name);
            if (entry == null) {
                entry = new FilterIndexEntry();
                nameToId.put(name, entry);
                matcher.createDictionaryEntry(name);
            }
            entry.addId(xid);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void load() {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(TestData.class.getResourceAsStream("/corpus_500k.txt"), "UTF8"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] e = line.split(",");
                addEntry(e[0], e[1]);
            }
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
