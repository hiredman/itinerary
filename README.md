# itinerary

itinerary is a replacement test runner and fixture mechanism for
clojure.test. 

## Why

clojure.test and clojure.test's fixtures work great, but when you have
a lot of integration like tests that requires lots of setup and tear
down, managing that efficiently to avoid long test run times is a
pain.

itinerary uses the fine grained fixture dependency information to
minimize running possibly expensive setup and tear downs in fixtures.

## Who

for me, not you. seriously most open source clojure projects I've seen
don't have the problem this is aimed at in their test suite, so you
most likely don't need this library.

## Usage

use the lein-itinerary lein plugin

## License

Copyright © 2013 Kevin Downey

Distributed under the Eclipse Public License, the same as Clojure.
