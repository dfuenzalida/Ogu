(ns ogu-lang.parser-test
    [:require [clojure.test :refer :all]
     [ogu-lang.parser :refer :all]])

(defn eval-mod [name]
      (parse-module {:eval true} name))

(defn eval-test [tests]
  (doseq [test tests]
    (println test)
    (let [f (:file test) v (:val test)]
      (is (= (eval-mod f) v)))))

(def test-alg
  [{:file "test/alg/e1.ogu" :val 233168}
   {:file "test/alg/e2.ogu" :val 4613732}
   {:file "test/alg/e3.ogu" :val 6857}
   {:file "test/alg/e4.ogu" :val 906609}
   {:file "test/alg/e5.ogu" :val 232792560}
   {:file "test/alg/e6.ogu" :val 25164150}
   {:file "test/alg/e7.ogu" :val 104743}
   {:file "test/alg/e8.ogu" :val 23514624000}
   {:file "test/alg/e9.ogu" :val 31875000}
   {:file "test/alg/e10.ogu" :val 142913828922}
   {:file "test/alg/ack.ogu" :val 10}
   {:file "test/alg/collatz.ogu" :val 66}
   {:file "test/alg/facts.ogu" :val true}
   {:file "test/alg/pi.ogu" :val false}
   {:file "test/alg/qsort.ogu" :val '(1 2 3 4 5 6 7 8 9)}])



(deftest test-1
  (testing "Check Euler"
    (eval-test test-alg)))

(def test-misc
  [{:file "test/misc/test0.ogu" :val nil}
   {:file "test/misc/test1.ogu" :val 11}
   {:file "test/misc/test2.ogu" :val 6}
   {:file "test/misc/test3.ogu" :val "foobar"}
   {:file "test/misc/test4.ogu" :val 9}
   {:file "test/misc/test5.ogu" :val 3999998000000}
   {:file "test/misc/test6.ogu" :val 8}
   {:file "test/misc/test7.ogu" :val 2}
   {:file "test/misc/test8.ogu" :val 620448401733239439360000N}
   {:file "test/misc/test9.ogu" :val 10100}
   {:file "test/misc/test10.ogu" :val '(40.0 20.0)}
   {:file "test/misc/test11.ogu" :val true}
   {:file "test/misc/test12.ogu" :val 1884.9555921538758}
   {:file "test/misc/test13.ogu" :val 7140}
   {:file "test/misc/test14.ogu" :val 166724149741}
   {:file "test/misc/test15.ogu" :val 60}
   {:file "test/misc/test16.ogu" :val '(4 7 6 8 11 4)}
   {:file "test/misc/test17.ogu" :val 10}
   {:file "test/misc/test18.ogu" :val 5040}
   {:file "test/misc/test19.ogu" :val nil}
   {:file "test/misc/test20.ogu" :val (for [p [\C \D \T \P] v [\A 2 3 4 5 6 7 8 9 10 \J \Q \K]] [v p])}
   {:file "test/misc/test21.ogu" :val false}
   {:file "test/misc/test22.ogu" :val true}
   {:file "test/misc/test23.ogu" :val 2000}
   {:file "test/misc/test24.ogu" :val '(0 1 2 3 4 5 6 7 8 9)}
   {:file "test/misc/test25.ogu" :val 20}
   {:file "test/misc/test26.ogu" :val ["area of shape: 314.1592653589793"  "area of rectangle: 5000" "area of shape: 200"]}
   {:file "test/misc/test27.ogu" :val "estas obeso, cuidado!"}
   {:file "test/misc/test28.ogu" :val "Hola Pedro"}
   ])

(deftest test-2
  (testing "Check Misc"
    (eval-test test-misc)))
