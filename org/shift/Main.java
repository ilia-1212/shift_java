package org.shift;

import java.io.*;
import java.util.*;
import java.util.logging.Logger;

/**
 * Утилита фильтрации содержимого файлов
 * Основной класс запуска приложения
 *
 * @author Ilia Sentiakov
 * @version 1.0
 */
public class Main {
    private static final String FILEWITHINT = "sample-integers.txt";
    private static final String FILEWITHFLOAT = "sample-floats.txt";
    private static final String FILEWITHSTRING = "sample-strings.txt";

    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());
    /**
     * Параметры запуска, передающиеся через список аргументов командной строки (Command-Line Arguments)
     * -o  путь для результатов
     * -p задает префикс имен выходных файлов
     * -a можно задать режим добавления в существующие файлы
     * -s | -f Выбор статистики (краткий | полный  вариант)
     * Пример:
     * {@code -s -o C:/dev -p frefix-1 w1.txt w2.txt w3.txt}
     */
    public static void main(String[] args)  {
        if (args.length == 0) {
            LOGGER.severe("Пустой список параметров запуска, выходим");
            System.exit(0);
        }
        Map<String, String> params = new HashMap<>();
        List<String> files = new ArrayList<>();
        fillParams(args, files, params);

        List<Long> longValues = new ArrayList<>();
        List<Double> doubleValues = new ArrayList<>();
        List<String> stringValues = new ArrayList<>();
        for (String fileName : files) {
            try {
                String dirName = (params.get("o") != null)?params.get("o"):System.getProperty("user.dir");
                fileName = dirName + "/" + fileName;
                BufferedReader BufferedReader = new BufferedReader(new FileReader(fileName));
                String str;
                while ((str = BufferedReader.readLine()) != null) {
                    if (fillList(longValues, str, Long.class)) continue;
                    if (fillList(doubleValues, str, Double.class)) continue;
                    fillList(stringValues, str, String.class);
                }
            } catch (IOException e) {
                LOGGER.severe("Ошибка обработки файла " + fileName + "; причина: "+ e.getMessage());
            }
        }

        try {
            writeResults(longValues, params,Long.class);
            writeResults(doubleValues, params,Double.class);
            writeResults(stringValues, params,String.class);
        } catch (IOException e) {
            LOGGER.severe("Ошибка записи результатов в файл; причина: "+ e.getMessage());
        }

        System.out.println("Вывод статистики");
        printStatistic(longValues, params, Long.class);
        printStatistic(doubleValues, params, Double.class);
        printStatistic(stringValues, params, String.class);
    }

    /**
     * Метод, заполняющий параметры запуска, разбирает массив строк и заполняет список файлов
     * и список параметров.
     *
     * @param args массив строк параметров запуска
     * @param files список файлов, полученный из разбора массива строк
     * @param params хэш-мэм, список параметров, полученный из разбора массива строк
     */
    private static void fillParams(String[] args, List<String> files, Map<String, String> params) {
        String key;
        String value;
        for (int idx = 0; idx < args.length; idx++) {
            key = args[idx];
            if (key.startsWith("-")) {
                key = key.substring(1);
                if (!args[idx + 1].startsWith("-")) {
                    value = args[idx + 1];
                } else {
                    value = "";
                }
                params.put(key, value);
            } else {
                if (!params.containsValue(args[idx])) {
                    files.add(args[idx]);
                }
            }
        }
    }

    /**
     * Метод, добавляет в список, значение из строкового парамета с типом, который передается как параметр
     *
     * @param values список значений
     * @param str строковое значение, которое приводится к типу класса
     * @param class_ название класса, для приведения типа, может иметь значения: Long, Double, String
     * @return возвращает true если- приведение типа прошло без ошибки, false - в ином случае
     */
    private static <T> boolean fillList(List<T> values, String str, Class<T> class_) {
        try {
            if (class_ == Long.class) {
                Long newLongValue;
                newLongValue = Long.valueOf(str);
                values.add((T) newLongValue);
            } else if (class_ == Double.class) {
                Double newDoubleValue;
                newDoubleValue = Double.valueOf(str);
                values.add((T) newDoubleValue);
            }  else if (class_ == String.class) {
                values.add((T) str);
            }
            return true;
        } catch (NumberFormatException e) {
            LOGGER.severe("Ошибка в разборе строки" + str + "; причина: "+ e.getMessage());
            return false;
        }
    }

    /**
     * Метод, записывает в список файлов FILEWITHINT, FILEWITHFLOAT, FILEWITHSTRING
     * значения из списка значений, с учетом списка парамтеров и указанного класса
     *
     * @param values список значений, для записи в файлы
     * @param params список параметров, которые учитывается при записи файлов
     * @param class_ название класса, для приведения типа, может иметь значения: Long, Double, String
     * @throws IOException если , при записи в файлы возникли ошибки
     */
    private static <T> void  writeResults(List<T> values, Map<String, String> params, Class<T> class_) throws IOException {
        boolean flagAppendResultFile = params.containsKey("a");
        String fileName;
        if (class_ == Long.class) {
            fileName = FILEWITHINT;
        } else if (class_ == Double.class) {
            fileName = FILEWITHFLOAT;
        } else if (class_ == String.class) {
            fileName = FILEWITHSTRING;
        } else {
            fileName = "";
        }
        if (values.size() > 0 | (!fileName.isEmpty()))  {
            String dirName = (params.get("o") != null)?params.get("o"):System.getProperty("user.dir");
            try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(dirName + "/" + params.get("p") + fileName, flagAppendResultFile))) {
                for (T val : values) {
                    bufferedWriter.write(val.toString());
                    bufferedWriter.newLine();
                }
            }
        }
    }

    /**
     * Метод, выводит статистику в разрезе типов данных Целые, Дробные, Строковые
     *
     * @param values список значений, для анализа статистики
     * @param params список параметров, которые учитывается для анализа статистики
     * @param class_ название класса, для приведения типа, может иметь значения: Long, Double, String
     */
    private static <T> void  printStatistic(List<T> values, Map<String, String> params, Class<T> class_) {
        if (params.containsKey("s") || params.containsKey("f")) {
            //Long
            long maxLongValue = Long.MIN_VALUE;
            long minLongValue = Long.MAX_VALUE;
            long summLong;
            long avgSummLong = 0;
            long valLong = 0L;
            // Double
            double maxDoubleValue = Double.MIN_VALUE;
            double minDoubleValue = Double.MAX_VALUE;
            double summDouble;
            double avgSummDouble = 0;
            double valDouble;
            // int
            int minIntegerValue = Integer.MAX_VALUE;
            int maxIntegerValue = Integer.MIN_VALUE;
            int valInt;

            for (T value_ : values) {
                if (class_ == Long.class) {
                    valLong = (Long) value_;
                    if (valLong <= minLongValue) minLongValue = valLong;
                    if (valLong >= maxLongValue) maxLongValue = valLong;
                    summLong = +valLong;
                    avgSummLong = summLong / values.size();
                } else if (class_ == Double.class) {
                    valDouble = (Double) value_;
                    if (valDouble <= minDoubleValue) minDoubleValue = valDouble;
                    if (valDouble >= maxDoubleValue) maxDoubleValue = valDouble;
                    summDouble = +valDouble;
                    avgSummDouble = summDouble / values.size();
                } else if (class_ == String.class) {
                    valInt = ((String) value_).length();
                    if (valInt <= minIntegerValue) minIntegerValue = valInt;
                    if (valInt >= maxIntegerValue) maxIntegerValue = valInt;

                }
            }

            if (class_ == String.class) {
                if (params.containsKey("s")) {
                    System.out.println("Строки, количество элементов: " + values.size());
                } else if (params.containsKey("f")) {
                    System.out.println("Строки, Минимальное число: " + minIntegerValue);
                    System.out.println("Строки, Максимальное число: " + maxIntegerValue);
                }
            } else if (class_ == Double.class) {
                if (params.containsKey("s")) {
                    System.out.println("Дробные числа, количество элементов: " + values.size());
                } else if (params.containsKey("f")) {
                    System.out.println("Дробные числа, Средняя сумма: " + avgSummDouble);
                    System.out.println("Дробные числа, Минимальное число: " + minDoubleValue);
                    System.out.println("Дробные числа, Максимальное число: " + maxDoubleValue);
                }
            } else if (class_ == Long.class) {
                if (params.containsKey("s")) {
                    System.out.println("Целые числа, количество элементов: " + values.size());
                } else if (params.containsKey("f")) {
                    System.out.println("Целые числа, Средняя сумма: " + avgSummLong);
                    System.out.println("Целые числа, Минимальное число: " + minLongValue);
                    System.out.println("Целые числа, Максимальное число: " + maxLongValue);
                }
            }
        }
    }
}