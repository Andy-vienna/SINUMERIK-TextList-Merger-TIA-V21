# TextList-Merger

Führt in einem TIA-Openness-Export (z.B. `PLCTextLists.xml`) alle TextLists
`SINUMERIK_UserAlarmsTextLib_<beliebiger Text>` zu einer TextList `SINUMERIK_UserAlarmsTextLib` zusammen.

- Die zusammengeführte Liste behält die Id der ersten Liste, alle Einträge bleiben erhalten.
- Die übrigen Listen und die Fingerprints aller betroffenen Listen entfallen.
- Sonst bleibt die Datei 1:1 (BOM, CRLF, Formatierung). Gelesen wird streamend.
- Ausgabe: gleicher Dateiname wie die Eingabe, im Zielordner.
  Ist der Zielordner der Quellordner, wird in den Unterordner `merged` geschrieben (wird bei Bedarf angelegt).

## Starten

GUI (Doppelklick oder):

    java -jar target\xml-merger-1.0.0.jar

Kommandozeile:

    java -jar target\xml-merger-1.0.0.jar PLCTextLists.xml <zielordner> [baseName]

## Bauen

    mvn package

