@echo off
SET /A countX = 0,0
SET /A countO = 0,0
SET /A count = 0

FOR /L %%i IN (0, 1, 10) DO (
	FOR /F "tokens=*" %%S IN ('java -jar game_engine.jar 0 game.quoridor.QuoridorGame 1234567890 5000 Agent game.quoridor.players.BlockRandomPlayer') DO echo Result is [%%S]
)
FOR /L %%i IN (0, 1, 10) DO (
	FOR /F "tokens=*" %%S IN ('java -jar game_engine.jar 0 game.quoridor.QuoridorGame 1234567890 5000 game.quoridor.players.BlockRandomPlayer Agent') DO echo Result is [%%S]
)