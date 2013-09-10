CREATE TABLE CELLS (
	SHEET_ID BIGINT NOT NULL,
	N_ROW BIGINT NOT NULL,
	N_COL BIGINT NOT NULL,
	T VARCHAR(10),
	S VARCHAR(10),
	V VARCHAR(100),
	
	PRIMARY KEY (SHEET_ID, N_ROW, N_COL)
)
