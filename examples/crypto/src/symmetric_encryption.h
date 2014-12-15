#ifndef SYMMETRIC_ENCRYPTION_H
#define SYMMETRIC_ENCRYPTION_H

#include "general.h"

void check_valid_symmetric_encrypted_item_chars_size(int cs_size);
  //@ requires true;
  //@ ensures  cs_size > (int) sizeof(char) + AES_IV_SIZE + (int) sizeof(int);

struct item *sym_encrypt(struct item *key, struct item *payload);
  /*@ requires [?f]world(?pub) &*&
               item(key, ?key_i) &*& 
               key_i == key_item(?s, ?count1, symmetric_key, ?info) &*&
               item(payload, ?pay_i) &*&
               generated_values(?principal, ?count2);
  @*/
  /*@ ensures  [f]world(pub) &*&
               item(key, key_item(s, count1, symmetric_key, info)) &*&
               item(payload, pay_i) &*&
               generated_values(principal, count2 + 1) &*&
               item(result, encrypted_item(?key_enc, ?pay_enc, ?entropy)) &*&
               true == if_no_collision(key_enc == key_i && pay_enc == pay_i);
  @*/

struct item *sym_decrypt(struct item *key, struct item *item);
  /*@ requires [?f]world(?pub) &*& item(item, ?i) &*&
               item(key, ?key_i) &*&
               key_i == key_item(?principal1, ?count1, symmetric_key, ?info);
  @*/
  /*@ ensures  [f]world(pub) &*& item(item, i) &*&
               item(key, key_i) &*&
               switch (i)
               {
                 case nonce_item(p0, c0, inc0, i0): return false;
                 case key_item(p0, c0, k0, i0): return false;
                 case data_item(d0): return false;
                 case hmac_item(k0, payload0): return false;
                 case encrypted_item(key0, pay0, ent0): return
                     item(result, ?result_i) &*&
                     true == if_no_collision(
                       key0 == key_item(principal1, count1, symmetric_key, info) &&
                       pay0 == result_i
                     );
                 case pair_item(f0, s0): return false;
               };
  @*/

#endif