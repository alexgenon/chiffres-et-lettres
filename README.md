# Play at french game ["Des Chiffres et des Lettres"](https://en.wikipedia.org/wiki/Des_chiffres_et_des_lettres)

## Architecture

Based on the Activator template [Spray Spark React] (https://www.typesafe.com/activator/template/spray-spark-react) that provided all
the building blocks I needed for this App (Spray - Spark - ReactJS). I later migrated from Spray to Akka-http.

The web server is based on akka and akka-http. akka-http is used for defining both the rest api and the routing to the UI (see `api`package).
Each request to a UI page is rendered using a Twirl template that basically calls the appropriate application written using Reactjs. 
The main idea here is to use Twirl for the general layout (menu, different components) and use Reactjs to define each individual app.

As for the Rest API, each request is processed by a dedicated Actor. So far, the actors below are needed (defined in `service` package) :
* Actor solving "le Compte est bon"
* Actor solving "Mot plus long"
* Actor logging stats on the processing of "Mot plus long" solver
* Live logging actor

### Compte est bon
The solution to the compte est bon is found using an algorithm inspired by the solution presented by Martin Odersky to the Water Pouring
problem in the final lecture of the course [Functional Programming Principles in Scala](https://www.coursera.org/course/progfun).

### Mot plus long
Instead of going for a brute force solution (i.e. checking each word in the dictionary if it can be written using the challenge letters), 
I tried a Locality Sensitive Hashing technique. Here, the buckets consist of set of 5 letters.
An indexing phase consists of reading each word in the dictionary, map it to the corresponding multiset of letters and then dispatch the word 
to each bucket that is a subset from this multiset. Then, each time we are challenged we take all 5 letters combinations from the challenge and check the corresponding buckets for candidates.
Spark is used here for both the indexing and solving.

I'm not sure that this approach is really efficient. The distribution of words in the buckets is far from being uniform (it follows more 
a power law distribution). Furthermore, a word will tend to be in many buckets (we have potentially 252 destination buckets 
for a 10 letters word).

To have some real data about how this technique is doing, I have defined a dedicated logging actor that is in charge in collecting some stats
on the solutions (how many buckets were checked, how many words in total, ...). Those data will be used to assess 
how efficient the technique is.

### Live logging
I wanted to play with WebSocket in sever push mode and an interesting use case was to setup a Live logging feature in the UI. 
Each request performed to any of the actor would be displayed live in the UI. 

On the UI side, there is a ReactJS component in charge of opening a WebSocket to the server and displaying all the messages sent.

On the server side, a dedicated route is defined using `handleWebsocketMessages`. The akka flow defined to process the WebSocket requests 
simply forwards the messages to/from the Livelogging actor. The Livelogging actor also receives messages from other actors that it then send 
to the WebSocket, via the materialized flow.

# run
```
activator run
```

this application uses sbt-revolver, which allows you to hot-deploy using

```
activator> ~ re-start
```
