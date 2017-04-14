## api (android <-> avr)

- example: `E`, `A1;80;75;1;-5;4;3;`, `R`

## commands

- `E` - enters data input mode //device will respond within 5seconds with "ok" //previous data is cleared
- `A<params>;` continues data input //max 10 entries in total //after each entry device will respond with "ok" or "error" if there was an error
- `R` - returns to normal displaying mode //no response

## parameters

`<daytime>;<cloudliness)>;<rainintensity>;<lightning>;<temperature>;<validFor>;<LC>`

- 1 - daytime; bool; 0 - day, 1 - night
- 2 - cloudliness; unsigned short; 0 - no clouds; 255 - max clouds
- 3 - rain intensity; unsigned short; 0 - no rain; 100 - max rain
- 4 - lightning; bool; 0 - no lightning; 1 - occasional lightning
- 5 - temperature; -99 to 99
- 6 - valid for; unsigned int; time in hours to present this entry; after this time next entry will be presented
- 7 - lc - lunar cycle; optional; unsigned short; only when daytime==1; values: 0(new moon), 1(full moon), 2(first quarter), 3(last quarter)