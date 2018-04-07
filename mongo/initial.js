conn = new Mongo();

db = conn.getDB("dev");

cursor = db.accounts.find({});

while (cursor.hasNext()) {
  printjson(cursor.next())
}
