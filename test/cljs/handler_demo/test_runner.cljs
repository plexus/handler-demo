(ns handler-demo.test-runner
  (:require
   [doo.runner :refer-macros [doo-tests]]
   [handler-demo.core-test]
   [handler-demo.common-test]))

(enable-console-print!)

(doo-tests 'handler-demo.core-test
           'handler-demo.common-test)
