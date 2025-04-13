import fp from "fastify-plugin";
import crypto from "node:crypto";

const encryptionKey = Buffer.from(process.env.encryption_key!, 'base64');
const iv_length = 12;
const algorithm = 'aes-256-gcm'
const defaultKeySkips = ["_id", "createdAt", "updatedAt"];

const encrypt = (value: string) => {
  const iv = crypto.randomBytes(iv_length);
  const cipher = crypto.createCipheriv(algorithm, encryptionKey, iv);
  const encrypted = Buffer.concat([cipher.update(value, 'utf8'), cipher.final()]);
  const tag = cipher.getAuthTag();
  return {
    content: encrypted.toString('hex'),
    iv: iv.toString('hex'),
    tag: tag.toString('hex')
  };
};

const decrypt = (content: string, iv: string, tag: string) => {
  const decipher = crypto.createDecipheriv(algorithm, encryptionKey, Buffer.from(iv, 'hex'));
  decipher.setAuthTag(Buffer.from(tag, 'hex'));
  const decrypted = Buffer.concat([decipher.update(Buffer.from(content, 'hex')), decipher.final()]);
  return decrypted.toString('utf8');
};

const deepEncrypt = (obj: any, skips: string[] = defaultKeySkips): any => {
  if(Array.isArray(obj)) {
    return obj.map(item => deepEncrypt(item, skips));
  }

  if(typeof obj === 'object' && obj !== null) {
    const encryptedObj: any = {};
    for(const key in obj) {
      if(skips.includes(key)) {
        encryptedObj[key] = obj[key];
      } else if(typeof obj[key] === 'string') {
        encryptedObj[key] = encrypt(obj[key]);
      } else {
        encryptedObj[key] = deepEncrypt(obj[key], skips);
      }
    }
    return encryptedObj;
  }
  if(typeof obj === 'string') {
    return encrypt(obj);
  }
  return obj;
}

const deepDecrypt = (obj: any, skips: string[] = defaultKeySkips): any => {
  if(Array.isArray(obj)) {
    return obj.map(item => deepDecrypt(item, skips));
  }

  if(typeof obj === 'object' && obj !== null && 'content' in obj) {
    return decrypt(obj.content, obj.iv, obj.tag);
  }

  if(typeof obj === 'object' && obj !== null) {
    const decryptedObj: any = {};
    for(const key in obj) {
      if(skips.includes(key)) {
        decryptedObj[key] = obj[key];
      } else if(typeof obj[key] === 'object' && obj[key] !== null && 'content' in obj[key]) {
        decryptedObj[key] = decrypt(obj[key].content, obj[key].iv, obj[key].tag);
      } else {
        decryptedObj[key] = deepDecrypt(obj[key], skips);
      }
    }
    return decryptedObj;
  }
  return obj;
}

declare module 'fastify' {
  interface FastifyInstance {
    encrypt: (obj: any, skips?: string[]) => any;
    decrypt: (obj: any, skips?: string[]) => any;
  }
}

export default fp(async (fastify, options) => {
  fastify.decorate('encrypt', deepEncrypt);
  fastify.decorate('decrypt', deepDecrypt);
});