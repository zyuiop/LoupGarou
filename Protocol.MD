Protocol Documentation
======================

Current protocol version : 1

## Some types definitions :

### String :

| int (4) | size of the following array            |
|---------|----------------------------------------|
| byte[]  | array of bytes representing the string |

## Packet structure

| int (4) | id of the packet |
|---------|------------------|
| ...     | packet content   |

## Server -> Client

### 0x01 : Game leave
### 0x02 : Game list
### 0x03 : Game join confirm
### 0x04 : Login response
### 0x05 : Message
### 0x06 : Set Phase
### 0x07 : Set Role
### 0x08 : Set state
### 0x09 : Set players
### 0x0A : Vote end
### 0x0B : Vote request

## Client -> Server

### 0x00 : Login
### 0xA1 : Create Game
### 0xA2 : Join Game
### 0xA3 : Vote
### 0xA4 : Refresh game list
### 0xA5 : Send Message