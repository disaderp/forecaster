## api (android <-> avr)

- example: `NEW:0,0.75,0.10,0,24,4;`, `ADD:1,0.80,0,75,1,-5,4;`

## commands

- `NEW:<params>;` deletes old data and starts new, should be used when updating settings with first data(which will be displayed from this moment)
- `ADD:<params>;` continues data input

## parameters

`<daytime>,<cloudliness)>,<rainintensity>,<lightning>,<temperature>,<validFor>,<LC>`

- 1 - daytime; accepted values: 0 or 1; 0 - day, 1 - night
- 2 - cloudliness; accepted values: from 0 to 1; pointCharacter: .; 0 - no clouds; 1 - max clouds
- 3 - rain intensity; accepted values: from 0 to 1; pointCharacter: .; 0 - no rain; 1 - max rain
- 4 - lightning; accepted values: 0 or 1; 0 - no lightning; 1 - occasional lightning
- 5 - temperature; accepted values: negative: from 99 to 10, from -9 to -1; 0; positive: 1-99
- 6 - valid for; accepted values: 1-8; time in hours to present this entry; after this time next entry will be presented
- 7 - lc - lunar cycle; optional; only when daytime==1; values: 0(new moon), 0.25(first quarter), 0.75(last quarter), 1(full moon)
