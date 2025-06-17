# 1 File Formats

Long 1

|   Field    | Data Size | Bit Index |
|:----------:|:---------:|:---------:|
| Y Position |  10 Bits  |     0     |  
| X Position |  11 Bits  |    10     |
| Z Position |  11 Bits  |    21     |
|   Owner    |  16 Bits  |    32     |
| Action ID  |  15 Bits  |    48     |
|   Hidden   |   1 Bit   |    63     |


Long 2

| Field | Data Size | Bit Index |
|:-----:|:---------:|:---------:|
| Data  |  48 Bits  |     0     | 
| Time  |  16 Bits  |    48     | 




Actions:

| Action Name | ID | DATA | 
|:-----------:|:--:|:----:|
|    NOOP     | 0  |  -   | 
| TIME HEADER | 1  |  -   |
|    BREAK    | 2  | 2.1 |
|    PLACE    | 3  | 2.1 |
|   REPLACE   | 4  |
|    DROP     | 5  |
|    KILL     | 6  |
|    SPAWN    | 7  |
|   IGNITE    | 8  | 
|    SHEAR    | 9  | 
|    TAKE     | 10 |
|     ADD     | 11 |
|    DECAY    | 12 | 
|  INTERACT   | 13 |
|  TELEPORT   | 14 | 
| MODIFY SIGN | 15 |
|  BULK TAKE  | 16 | 
|  BULK ADD   | 17 | 


# 2 Data Formats

## 2.1 Block Format

Block
