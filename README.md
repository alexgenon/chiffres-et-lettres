# Play at french game ["Des Chiffres et des Lettres"](https://en.wikipedia.org/wiki/Des_chiffres_et_des_lettres)

## Architecture

Based on the Activator template [Spray Spark React] (https://www.typesafe.com/activator/template/spray-spark-react) that provided all
the building blocks I needed for this App (Spray - Spark - ReactJS). I later migrated from Spray to Akka-http. 


# run
```
activator run
```

this application uses sbt-revolver, which allows you to hot-deploy spray using

```
activator> ~ re-start
```
