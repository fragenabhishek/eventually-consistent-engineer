package com.fragenabhishek.designpatterns.creational;

import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class Test {
    public static void main(String[] args) {

        String sentence = "The quick brown fox jumps right over the little lazy dog.";

        System.out.println(
                Arrays.stream(sentence.split(" "))
                        .filter(w -> !w.isEmpty())
                        .filter(w -> countVowels(w) >= 2)
                        .collect(Collectors.toList())
        );

//        input.stream()
//                .filter(x -> {
//                    try {
//                        Integer.parseInt(x);
//                        return true;
//                    } catch (NumberFormatException e) {
//                        return false;
//                    }
//                })
//                .map(Integer::parseInt)
//                .forEach(System.out::print);


//        System.out.println(
//                Arrays.stream(s.split(" "))
//                        .sorted((a,b) -> b.length() - a.length())
//                        .skip(1)
//                        .findFirst().orElse(null)
//        );


//        System.out.println(
//                non.chars()
//                        .mapToObj(ch->(char)ch)
//                        .collect(Collectors.groupingBy(x->x, LinkedHashMap:: new,Collectors.counting()))
//                        .entrySet().stream()
//                        .filter(m->m.getValue() == 1)
//                        .map(Map.Entry :: getKey)
//                        .findFirst()
//                        .orElse(null)
//
//        );

//        List<Integer> nums = List.of(1, 3, 5, 7, 9, 2, 4);
//        System.out.println(
//                nums.stream()
//                        .mapToInt(x->(x*x))
//                        .filter(x->x>25)
//                        .average()
//
//        );



//        List<String> input = List.of("abc", "aab", "defg", "xyz");
//
//        System.out.println(input.stream()
//                .filter(s -> s.chars().distinct().count() == s.length())
//                .sorted((a,b)-> b.length() - a.length())
//                .findFirst()
//                .orElse("")
//        );

//        List<String> words = List.of("apple", "avocado", "banana", "blueberry", "cherry");
//
//        System.out.println(
//                words.stream()
//                        .collect(Collectors.groupingBy(x->x.charAt(0)))
//        );

        String tc = "abracaddddddabra";

//        char mostFrequent = tc.chars()
//                .mapToObj(c -> (char) c)
//                .collect(Collectors.groupingBy(c -> c, Collectors.counting()))
//                .entrySet().stream()
//                .max(Map.Entry.comparingByValue())
//                .map(Map.Entry::getKey)
//                .orElseThrow(() -> new IllegalArgumentException("Empty string"));
//
//        System.out.println(mostFrequent); // prints 'd' reliably





    }

    private static long countVowels(String w) {
        return w.toLowerCase()
                .chars()
                .mapToObj(c->(char)c)
                .filter(c->"aeiou".indexOf(c) >= 0)
                .count();
    }
}
