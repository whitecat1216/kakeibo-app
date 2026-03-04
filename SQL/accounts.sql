CREATE TABLE accounts (
  id SERIAL PRIMARY KEY,
  date DATE NOT NULL,
  type VARCHAR(10) NOT NULL, -- 'income' or 'expense'
  category VARCHAR(50) NOT NULL,
  item VARCHAR(100),
  amount INTEGER NOT NULL,
  memo TEXT,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);