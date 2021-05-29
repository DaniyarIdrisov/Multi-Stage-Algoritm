package ru.kpfu.itis.datamining.daniayr.idrisov.multistage;

import ru.kpfu.itis.datamining.daniayr.idrisov.models.Doubleton;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class MultiStageAlgorithm {

    private static final String ITEMS_PATH = "src/main/resources/items.txt";

    private static final String BASKETS_PATH = "src/main/resources/baskets.txt";


    private int support;

    private List<List<Integer>> idBasketsList;

    private Map<Integer, Integer> idItemsCountMap;

    private Map<Integer, String> idItemsMap;

    public MultiStageAlgorithm() throws IOException {
        Scanner scanner = new Scanner(System.in);
        this.itemsCount();
        this.generatingBaskets();
        System.out.println("Enter support:");
        this.support = scanner.nextInt();
        this.idItemsCountMap = new HashMap<>();
        this.idItemsMap = new HashMap<>();
        this.idBasketsList = basketsListToMapsAndCreatingIdBasketsList(getBasketsList());
        this.getItemsSatisfyingConditions();
    }

    public static void main(String[] args) throws IOException {
        new MultiStageAlgorithm();
    }

    private void itemsCount() throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new FileReader(ITEMS_PATH));
        int count = 0;
        while (bufferedReader.ready()) {
            bufferedReader.readLine();
            count++;
        }
        System.out.println("----------------");
        System.out.println("Items count: " + count);
        System.out.println("----------------");
    }

    private void generatingBaskets() throws IOException {
        Scanner scanner = new Scanner(System.in);
        BufferedReader bufferedReader = new BufferedReader(new FileReader(ITEMS_PATH));
        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(BASKETS_PATH));
        System.out.println("Enter baskets count:");
        int basketsCount = scanner.nextInt();
        System.out.println("Enter maximum basket size");
        int maxBasketSize = scanner.nextInt();
        System.out.println("---------------------");
        System.out.println("Generating baskets...");
        System.out.println("---------------------");
        List<String> items = new ArrayList<>();
        while (bufferedReader.ready()) {
            String item = bufferedReader.readLine();
            items.add(item);
        }
        for (int i = 0; i < basketsCount; i++) {
            int basketSize = (int) (2 + Math.random() * (maxBasketSize - 1.5));
            for (int j = 0; j < basketSize; j++) {
                int itemId = (int) (Math.random() * items.size());
                bufferedWriter.write(items.get(itemId) + " ");
            }
            bufferedWriter.write("\n");
        }
        bufferedWriter.flush();
    }

    private List<List<String>> getBasketsList() throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new FileReader(new File(BASKETS_PATH)));
        List<List<String>> basketList = new ArrayList<>();
        while (bufferedReader.ready()) {
            String[] items = bufferedReader.readLine().split(" ");
            List<String> basketItems = Arrays.stream(items).collect(Collectors.toList());
            basketList.add(new ArrayList<>(basketItems));
        }
        return basketList;
    }

    private List<List<Integer>> basketsListToMapsAndCreatingIdBasketsList(List<List<String>> basketsList) {
        Map<String, Integer> localMap = new HashMap<>();
        List<List<Integer>> idBasketsList = new ArrayList<>();
        int count = 1;
        for (List<String> basketItems : basketsList) {
            for (String item : basketItems) {
                if (!localMap.containsKey(item)) {
                    localMap.put(item, count);
                    idItemsMap.put(count, item);
                    count++;
                }
                if (!idItemsCountMap.containsKey(localMap.get(item))) {
                    idItemsCountMap.put(localMap.get(item), 0);
                }
                int thisItemCount = idItemsCountMap.get(localMap.get(item));
                idItemsCountMap.put(localMap.get(item), thisItemCount + 1);
            }
        }
        for (List<String> basketItems : basketsList) {
            List<Integer> idItemsBasket = new ArrayList<>();
            for (String item : basketItems) {
                idItemsBasket.add(localMap.get(item));
            }
            idBasketsList.add(idItemsBasket);
        }
        return idBasketsList;
    }

    private List<Set<Doubleton>> calculatingDoubletonValues(List<Doubleton> dList, Map<Integer, Integer> idItemsCountMap, int coefficient) {
        List<Set<Doubleton>> dSetsList = new ArrayList<>();
        for (int i = 0; i < idItemsCountMap.size(); i++) {
            dSetsList.add(new HashSet<>());
        }
        for (Doubleton doubleton : dList) {
            int count = 0;
            int dValue = (doubleton.getOne() + coefficient * doubleton.getTwo()) % idItemsCountMap.size();
            Iterator<Doubleton> iterator = dSetsList.get(dValue).iterator();
            while (iterator.hasNext()) {
                Doubleton thisD = iterator.next();
                if (thisD.getOne().equals(thisD.getOne()) && thisD.getTwo().equals(thisD.getTwo())) {
                    count = thisD.getCount();
                }
            }
            doubleton.setCount(count + 1);
            dSetsList.get(dValue).add(doubleton);
        }
        return dSetsList;
    }

    private List<Doubleton> getDoubletonList(List<Set<Doubleton>> dSetsList, List<Doubleton> dList) {
        Set<Doubleton> dSet = new HashSet<>();
        List<Doubleton> thisDList = new ArrayList<>();
        for (Set<Doubleton> thisDSet : dSetsList) {
            Iterator<Doubleton> iterator = thisDSet.iterator();
            int sum = 0;
            while (iterator.hasNext()) {
                Doubleton doubleton = iterator.next();
                sum += doubleton.getCount();
            }
            if (sum >= support) {
                dSet.addAll(thisDSet);
            }
        }
        for (Doubleton doubleton : dList) {
            if (dSet.stream().anyMatch(k -> k.getOne() == doubleton.getOne() && k.getTwo() == doubleton.getTwo())) {
                thisDList.add(doubleton);
            }
        }
        return thisDList;
    }

    private List<Doubleton> getFinalValues() {
        List<Doubleton> dList = new ArrayList<>();
        List<Doubleton> finalDList = new ArrayList<>();
        Set<Doubleton> finalDSet = new HashSet<>();
        for (List<Integer> idItemsList : idBasketsList) {
            for (int i = 0; i < idItemsList.size(); i++) {
                for (int j = i + 1; j < idItemsList.size(); j++) {
                    Doubleton doubleton = new Doubleton(idItemsList.get(i), idItemsList.get(j));
                    dList.add(doubleton);
                }
            }
        }
        List<Set<Doubleton>> dSetsList = calculatingDoubletonValues(dList, idItemsCountMap, 1);
        List<Doubleton> thisDList = getDoubletonList(dSetsList, dList);
        dSetsList = calculatingDoubletonValues(thisDList, idItemsCountMap,  2);
        thisDList = getDoubletonList(dSetsList, dList);
        for (Doubleton doubleton : thisDList) {
            int one = idItemsCountMap.get(doubleton.getOne());
            int two = idItemsCountMap.get(doubleton.getTwo());
            if (one >= support && two >= support) {
                if (finalDSet.stream().noneMatch(k -> k.getOne() == doubleton.getOne() && k.getTwo() == doubleton.getTwo())) {
                    finalDSet.add(doubleton);
                    finalDList.add(doubleton);
                }
            }
        }
        return finalDList;
    }

    public void getItemsSatisfyingConditions() {
        List<Doubleton> dList = getFinalValues();
        List<String> finalValues = new ArrayList<>();
        for (int i = 1; i <= idItemsCountMap.size(); i++) {
            if (idItemsCountMap.get(i) >= support) {
                String str = idItemsMap.get(i);
                finalValues.add(str);
            }
        }
        for (Doubleton doubleton : dList) {
            String dItems = "{" + idItemsMap.get(doubleton.getOne()) + ", " + idItemsMap.get(doubleton.getTwo()) + "}";
            finalValues.add(dItems);
        }
        System.out.println("----------------------------");
        System.out.println("Items satisfying conditions:");
        System.out.println("----------------------------");
        for (String Items: finalValues) {
            System.out.println(Items);
        }
        System.out.println("--------------------------------------");
        System.out.println("Items satisfying conditions count: " + finalValues.size());
        System.out.println("--------------------------------------");
    }

}
