## Objective

This is a project done as part of NTU SC3020 Database Design Principles.

The objective is to attempt to implement a simplified DBMS, including components like disk, block/page, B+ tree, record, write ahead log, and crash recovery.

In the implementation I tried to follow practices and designs used by commerical DBMSs where possible.

A lot of ideas are inspired from Designing Data Intensive Applications.


## Key design decisions

1. Append-only log mechanism: Every operation performed on the database is appended sequentially in a log file. This gives us benefits like making use of sequential writes and immutability.
2. 'Tombstone' records: Deleting records append a tombstone instead of deleting the record in-place
3. Compaction process: To ensure the size of the append-only log is manageable, a compaction process is run when its size exceeds some threshold. This involves removing redundant entries(keep the latest entry only) or removing the entry entirely(tombstone)
4. Write ahead log: To mimic commercial DBMS, writes are written to WAL before they are written to disk. The intent is if there's a crash the DBMS can recover to a consistent state by looking at the WAL. Note that this is just a bonus as it does not make sense since transactions are not supported in this simplified DBMS.

## Specifications

### Disk

- Disk is just a byte[] of 500MB. However we can think of it as having many Blocks
- On disk we are representing data as an append-only log to utilize sequential writes for better performance. To prevent out-of-memory issues we will run a compaction process when the size of data is 90% of the limit(500mb)

### Block

- A block is a logical unit representing some slice of the Disk.
- Each block is 200 Bytes
- Each block has a 4 Byte block header containing an integer = # of records in the block currently
- Each block can hold a maximum of 9 Records

### Record

- Each Record is 24 Bytes = 2 Byte header + 2 Byte padding + 10 Byte String + 2 Byte padding + 4 Byte Float + 4 Byte Integer
- Each record header contains a short = 0/1, which indicates whether a record is a tombstone(see below)

## Insert/Delete/Update/Read

- Inserting records are append-only to make use of sequential writes. To ensure that a lookup of a record is log(n) and not O(n) we create a default B tree index on the primary key(which is what real RDBMS do in practice)
- Deleting records will not be executed immediately. Instead a 'tombstone'(borrowed term from distributed systems) will be appended(and the index's record pointer will point to this tombstone).
- Updating records are essentially delete + insert. A tombstone displaces the original record. Then the updated record is appended at the end to make use of sequential writes. Finally, the index structure is updated to hold the new (block #, record # in block).
- Reading records by primary key involve retrieving the (block #, record # in block) from the default B tree. If it's a miss or the lookup is done on a non-primary column then a sequential O(n) look up is executed.

## Crash Recovery

- Crash recovery is implemented using a (Write-Ahead Log)[https://www.postgresql.org/docs/current/wal-intro.html]
- To match closer to real DBMS, we write to a binary log file.
- Only need to log INSERT and DELETE operations, since UPDATE operations are just DELETE + INSERT
