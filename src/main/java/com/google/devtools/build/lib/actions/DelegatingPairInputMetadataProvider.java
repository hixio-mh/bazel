// Copyright 2023 The Bazel Authors. All rights reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.devtools.build.lib.actions;

import com.google.common.collect.ImmutableList;
import com.google.devtools.build.lib.skyframe.TreeArtifactValue;
import java.io.IOException;
import java.util.LinkedHashSet;
import javax.annotation.Nullable;

/** A {@link InputMetadataProvider} implementation that consults two others in a given order. */
public final class DelegatingPairInputMetadataProvider implements InputMetadataProvider {

  private final InputMetadataProvider primary;
  private final InputMetadataProvider secondary;

  public DelegatingPairInputMetadataProvider(
      InputMetadataProvider primary, InputMetadataProvider secondary) {
    this.primary = primary;
    this.secondary = secondary;
  }

  @Override
  public FileArtifactValue getInputMetadataChecked(ActionInput input)
      throws IOException, MissingDepExecException {
    FileArtifactValue metadata = primary.getInputMetadata(input);
    return (metadata != null) && (metadata != FileArtifactValue.MISSING_FILE_MARKER)
        ? metadata
        : secondary.getInputMetadataChecked(input);
  }

  @Nullable
  @Override
  public TreeArtifactValue getTreeMetadata(ActionInput actionInput) {
    TreeArtifactValue metadata = primary.getTreeMetadata(actionInput);
    return metadata != null ? metadata : secondary.getTreeMetadata(actionInput);
  }

  @Override
  @Nullable
  public RunfilesArtifactValue getRunfilesMetadata(ActionInput input) {
    RunfilesArtifactValue result = primary.getRunfilesMetadata(input);
    return result != null ? result : secondary.getRunfilesMetadata(input);
  }

  @Override
  public ImmutableList<RunfilesTree> getRunfilesTrees() {
    LinkedHashSet<RunfilesTree> result = new LinkedHashSet<>();
    result.addAll(primary.getRunfilesTrees());
    result.addAll(secondary.getRunfilesTrees());
    return ImmutableList.copyOf(result);
  }

  @Override
  public ActionInput getInput(String execPath) {
    ActionInput input = primary.getInput(execPath);
    return input != null ? input : secondary.getInput(execPath);
  }
}
