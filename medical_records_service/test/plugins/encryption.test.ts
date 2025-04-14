import Fastify, { FastifyInstance } from "fastify";
import { beforeEach, describe, it } from "node:test";
import * as assert from 'node:assert/strict';
import encryption from "../../src/plugins/encryption";

describe("Encryption Plugin", () => {
  let fastify: FastifyInstance;
  const expectedEncryptionObjectKeys = ["content", "iv", "tag"].sort();

  beforeEach(async () => {
    fastify = Fastify();
    await fastify.register(encryption, {});
    await fastify.ready();
  });

  it("should register the encryption plugin", async () => {
    assert.equal(typeof fastify.encrypt, "function");
    assert.equal(typeof fastify.decrypt, "function");
  });

  it("should encrypt and decrypt a string", () => {
    const original = "Hello, World!";
    const encrypted = fastify.encrypt(original);
    const decrypted = fastify.decrypt(encrypted);

    assert.deepStrictEqual(
      Object.keys(encrypted).sort(),
      expectedEncryptionObjectKeys,
      "Encrypted object should contain content, iv, and tag keys"
    ),
    assert.deepStrictEqual(decrypted, original);
  });

  it("should deep encrypt and decrypt an object", () => {
    const original = { name: "Alice", age: 30, nested: { city: "Wonderland" } };
    const encrypted = fastify.encrypt(original);
    const decrypted = fastify.decrypt(encrypted);
    
    assert.deepStrictEqual(
      Object.keys(encrypted.name).sort(),
      expectedEncryptionObjectKeys,
      "Encrypted object should contain content, iv, and tag keys"
    );
    assert.deepStrictEqual(
      Object.keys(encrypted.nested.city).sort(),
      expectedEncryptionObjectKeys,
      "Encrypted object should contain content, iv, and tag keys"
    );
    assert.deepStrictEqual(decrypted, original);
    // only encrypts string values
    assert.strictEqual(original.age, encrypted.age);
  });

  it("should handle arrays correctly", () => {
    const original = [{ name: "Bob" }, { name: "Charlie" }];
    const encrypted = fastify.encrypt(original);
    const decrypted = fastify.decrypt(encrypted);

    assert.deepStrictEqual(
      Object.keys(encrypted[0].name).sort(),
      expectedEncryptionObjectKeys,
      "Encrypted object should contain content, iv, and tag keys"
    );
    assert.deepStrictEqual(
      Object.keys(encrypted[1].name).sort(),
      expectedEncryptionObjectKeys,
      "Encrypted object should contain content, iv, and tag keys"
    );
    assert.deepStrictEqual(decrypted, original);
  });

  it("should handle empty objects and arrays", () => {
    const original = { countries: ["Ghana", "Nigeria"], name: "bob", nested: { city: "Accra"} };
    const encrypted = fastify.encrypt(original);
    const decrypted = fastify.decrypt(encrypted);

    assert.deepStrictEqual(
      Object.keys(encrypted.countries[0]).sort(),
      expectedEncryptionObjectKeys,
      "Encrypted object should contain content, iv, and tag keys"
    );
    assert.deepStrictEqual(
      Object.keys(encrypted.countries[1]).sort(),
      expectedEncryptionObjectKeys,
      "Encrypted object should contain content, iv, and tag keys"
    );
    assert.deepStrictEqual(
      Object.keys(encrypted.nested.city).sort(),
      expectedEncryptionObjectKeys,
      "Encrypted object should contain content, iv, and tag keys"
    );
    assert.deepStrictEqual(
      Object.keys(encrypted.name).sort(),
      expectedEncryptionObjectKeys,
      "Encrypted object should contain content, iv, and tag keys"
    );
    assert.deepStrictEqual(decrypted, original);
  });

  it("should ignore selected keys", () => {
    const original = { name: "Alice", age: 30, nested: { city: "Wonderland" } };
    const skips = ["name"];
    const encrypted = fastify.encrypt(original, skips);
    const decrypted = fastify.decrypt(encrypted, skips);

    assert.deepStrictEqual(
      Object.keys(encrypted.nested.city).sort(),
      expectedEncryptionObjectKeys,
      "Encrypted object should contain content, iv, and tag keys"
    );
    assert.strictEqual(encrypted.name, original.name);
    assert.deepStrictEqual(decrypted, original);
    // only encrypts string values
    assert.strictEqual(original.age, encrypted.age);
  });
});