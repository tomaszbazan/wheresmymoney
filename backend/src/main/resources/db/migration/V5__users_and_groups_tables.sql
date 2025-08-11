-- Create groups table
CREATE TABLE groups
(
    id          UUID PRIMARY KEY,
    name        VARCHAR(100)             NOT NULL,
    description TEXT,
    created_by  UUID                     NOT NULL,
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create users table
CREATE TABLE users
(
    id               UUID PRIMARY KEY,
    external_auth_id VARCHAR(255)             NOT NULL UNIQUE,
    email            VARCHAR(255)             NOT NULL UNIQUE,
    display_name     VARCHAR(100)             NOT NULL,
    group_id         UUID                     NOT NULL,
    created_at       TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_login_at    TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    joined_group_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (group_id) REFERENCES groups (id) ON DELETE RESTRICT
);

-- Create group_members table (for maintaining group membership)
CREATE TABLE group_members
(
    group_id UUID NOT NULL,
    user_id  UUID NOT NULL,
    PRIMARY KEY (group_id, user_id),
    FOREIGN KEY (group_id) REFERENCES groups (id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

-- Create group_invitations table
CREATE TABLE group_invitations
(
    id               UUID PRIMARY KEY,
    group_id         UUID                     NOT NULL,
    invitee_email    VARCHAR(255)             NOT NULL,
    invitation_token VARCHAR(255)             NOT NULL UNIQUE,
    invited_by       UUID                     NOT NULL,
    status           VARCHAR(20)              NOT NULL DEFAULT 'PENDING',
    created_at       TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at       TIMESTAMP WITH TIME ZONE NOT NULL,
    FOREIGN KEY (group_id) REFERENCES groups (id) ON DELETE CASCADE,
    FOREIGN KEY (invited_by) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT check_invitation_status CHECK (status IN ('PENDING', 'ACCEPTED', 'EXPIRED'))
);

-- Create indexes for better performance
CREATE INDEX idx_users_external_auth_id ON users (external_auth_id);
CREATE INDEX idx_users_email ON users (email);
CREATE INDEX idx_users_group_id ON users (group_id);
CREATE INDEX idx_group_invitations_token ON group_invitations (invitation_token);
CREATE INDEX idx_group_invitations_email ON group_invitations (invitee_email);
CREATE INDEX idx_group_invitations_group_id ON group_invitations (group_id);
CREATE INDEX idx_group_invitations_status ON group_invitations (status);
CREATE INDEX idx_group_invitations_expires_at ON group_invitations (expires_at);