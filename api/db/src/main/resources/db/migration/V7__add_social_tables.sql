CREATE TABLE friends (
    account_id INTEGER NOT NULL,
    friend_account_id INTEGER NOT NULL,
    friend_display_name_snapshot TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (account_id, friend_account_id),
    FOREIGN KEY (account_id) REFERENCES accounts(id) ON DELETE CASCADE,
    FOREIGN KEY (friend_account_id) REFERENCES accounts(id) ON DELETE CASCADE,
    CHECK (account_id != friend_account_id)
);

CREATE INDEX idx_friends_account_id ON friends(account_id);
CREATE INDEX idx_friends_friend_account_id ON friends(friend_account_id);

CREATE TABLE ignores (
    account_id INTEGER NOT NULL,
    ignored_account_id INTEGER NOT NULL,
    ignored_display_name_snapshot TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (account_id, ignored_account_id),
    FOREIGN KEY (account_id) REFERENCES accounts(id) ON DELETE CASCADE,
    FOREIGN KEY (ignored_account_id) REFERENCES accounts(id) ON DELETE CASCADE,
    CHECK (account_id != ignored_account_id)
);

CREATE INDEX idx_ignores_account_id ON ignores(account_id);
CREATE INDEX idx_ignores_ignored_account_id ON ignores(ignored_account_id);
